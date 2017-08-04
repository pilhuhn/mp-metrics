/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bsd.mp_metrics.it;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.core.Is.is;

import de.bsd.mp_metrics.impl.Main;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.path.json.JsonPath;
import java.util.List;
import java.util.Map;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

/**
 * @author hrupp
 */
@RunAsClient
@RunWith(Arquillian.class)
public class MpMetricsIT  {

  private static final String APPLICATION_JSON = "application/json";
  private static final String TEXT_PLAIN = "text/plain";

  private static final Header wantJson = new Header("Accept",APPLICATION_JSON);
  private static final Header wantPrometheusFormat = new Header("Accept",TEXT_PLAIN);

  @Deployment
  public static Archive createDeployment() throws Exception {
    JAXRSArchive deployment = Main.getJaxrsArchive();
    deployment.addAsResource(new ClassLoaderAsset("de/bsd/mp_metrics/mapping.yml"), "de/bsd/mp_metrics/mapping.yml");
    return  deployment;
  }

  @Test
  public void testBadSubTree() {
    when().get("http://localhost:8080/metrics/bad-tree")
        .then()
        .statusCode(404);
  }

  @Test
  public void testListsAllJson() {
    given()
        .header(wantJson)
    .when()
        .get("http://localhost:8080/metrics")
        .then()
        .statusCode(200)
        .and().contentType(APPLICATION_JSON);

    Map response =
        given()
            .header(wantJson)
        .when()
            .get("http://localhost:8080/metrics")
        .as(Map.class);

    assert response.containsKey("base");
    assert response.containsKey("vendor");
    assert response.containsKey("application");
  }

  @Test
  public void testListsAllPrometheus() {
    given()
        .header(wantPrometheusFormat)
    .when()
        .get("http://localhost:8080/metrics")
        .then()
        .statusCode(200)
        .and().contentType("text/plain");
  }


  @Test
  public void testBase() {
    given()
        .header("Accept", APPLICATION_JSON)
    .when().get("http://localhost:8080/metrics/base")
        .then()
        .statusCode(200)
        .and().contentType(MpMetricsIT.APPLICATION_JSON)
        .and()
        .body(containsString("totalStartedThreadCount"));
  }

  @Test
  public void testBasePrometheus() {
    given()
            .header("Accept","text/plain")
    .when().get("http://localhost:8080/metrics/base")
        .then()
        .statusCode(200)
        .and().contentType("text/plain")
        .and()
        .body(containsString("# TYPE base:total_started_thread_count"),
              containsString("base:total_started_thread_count{tier=\"integration\"}"));
  }

  @Test
  public void testBaseAttributeJson() {
    given()
        .header(wantJson)
    .when().get("http://localhost:8080/metrics/base/totalStartedThreadCount")
        .then()
        .statusCode(200)
        .and().contentType(MpMetricsIT.APPLICATION_JSON)
        .and()
        .body(containsString("totalStartedThreadCount"));
  }


  @Test
  public void testBaseAttributePrometheus() {
    given()
            .header("Accept","text/plain")
    .when().get("http://localhost:8080/metrics/base/totalStartedThreadCount")
        .then()
        .statusCode(200)
        .and().contentType("text/plain")
        .and()
        .body(containsString("# TYPE base:total_started_thread_count"),
              containsString("base:total_started_thread_count{tier=\"integration\"}"));
  }

  @Test
  public void testVendor() {
    given()
        .header(wantJson)
        .get("http://localhost:8080/metrics/vendor")
        .then()
        .contentType(MpMetricsIT.APPLICATION_JSON)
        .and()
        .body(containsString("mscLoadedModules"));

  }

  // [{"name":"msc-loaded-modules","displayName":"msc-loaded-modules",
  //  "mbean":"jboss.modules:type=ModuleLoader,name=BootModuleLoader-2/LoadedModuleCount",
  //  "description":"Number of loaded modules","type":"gauge","unit":"none"}]
  @Test
  public void testVendorMetadata() {
    given()
            .header(wantJson)
    .options("http://localhost:8080/metrics/vendor")
        .then().statusCode(200)
        .and().contentType(MpMetricsIT.APPLICATION_JSON)
        .and().body("[0].name", is("mscLoadedModules"));
  }

  @Test
  public void testVendorMetadata2() {
    given()
            .header(wantJson)
    .options("http://localhost:8080/metrics/vendor")
        .then().statusCode(200)
        .and().contentType(MpMetricsIT.APPLICATION_JSON)
        .and().body("name", hasItem("BufferPool_used_memory_mapped"));
  }

  @Test
  public void testVendorMetadata3() {
    JsonPath jsonPath =
      given()
              .header(wantJson)
      .options("http://localhost:8080/metrics/vendor")
          .then().statusCode(200)
          .and().contentType(MpMetricsIT.APPLICATION_JSON)
          .extract().body().jsonPath();

    //jsonPath.getMap("find {it.name == 'BufferPool_used_memory_direct'}");
    // jsonPath.getString("find {it.name == 'BufferPool_used_memory_direct'}.name");

    Map<String,String> directPool = jsonPath.getMap("find {it.name == 'BufferPool_used_memory_direct'}");
    assert directPool.get("displayName").equals("BufferPool_used_memory_direct");
    assert directPool.get("description").equals("The memory used by the pool: direct");
  }

  @Test
  public void testApplicationMetadataOkJson() {
    given()
        .header(wantJson)
    .options("http://localhost:8080/metrics/application")
        .then().statusCode(200)
        .and().contentType(MpMetricsIT.APPLICATION_JSON)
      ;
  }

  @Test
  public void testApplicationMetadata() {
    List<Map> body = RestAssured.options("http://localhost:8080/metrics/application")
        .as(List.class);

    assert body.size()==2;

    for (Map entry : body) {
      String tags = (String) entry.get("tags");
      if (entry.get("name").equals("hello")) {

        assert entry.get("unit").equals("none");
        assert tags.contains("app=\"shop\"");
      }
      else if (entry.get("name").equals("ola")) {
        assert entry.get("unit").equals("none");
        assert tags.contains("app=\"ola\"");
      }
      else {
        throw new RuntimeException("Unexpected body element");
      }
    }
  }

  @Test
  public void testApplicationsData() {
    given()
        .header(wantJson)
    .when().get("http://localhost:8080/metrics/application")
        .then().statusCode(200)
        .and().contentType(MpMetricsIT.APPLICATION_JSON)
        .and()
        .body(containsString("ola"),containsString("hello"));

  }

  @Test
  public void testApplicationsDataHello() {
    // Get the counter
    int count = when().get("http://localhost:8080/demo/count")
        .then().statusCode(200)
        .extract().path("hello");


    given()
          .header(wantJson)
    .when().get("http://localhost:8080/metrics/application/hello")
        .then().statusCode(200)
        .and().contentType(MpMetricsIT.APPLICATION_JSON)
        .and()
        .body(containsString("\"hello\":"+count));
  }

  @Test
  public void testApplicationsDataHello2() {

    // Get the counter
    int count = when().get("http://localhost:8080/demo/count")
        .then().statusCode(200)
        .extract().path("hello");

    // Call hello world to bump the counter
    when().get("http://localhost:8080/demo/hello")
        .then().statusCode(200);
    count++;


    // Compare with what we got from the metrics api

    given()
            .header("Accept", MpMetricsIT.APPLICATION_JSON)
    .when().get("http://localhost:8080/metrics/application/hello")
        .then().statusCode(200)
        .and().contentType(MpMetricsIT.APPLICATION_JSON)
        .and()
        .body(containsString("\"hello\":"+count));
  }

  @Test
  public void testUnitScalingPromMetricsName() {

    given()
        .header("Accept","text/plain")
    .when()
        .get("http://localhost:8080/metrics/vendor/mscLoadedModulesTime")
    .then()
        .statusCode(200)
        .and()
        .body(containsString("msc_loaded_modules_time_seconds"));
  }

  @Test
  public void testUnitScalingPromMetricsValue() {

    //  get value via unscaled json
    int val = when().get("http://localhost:8080/metrics/vendor/mscLoadedModulesTime")
         .then().statusCode(200)
         .extract().path("mscLoadedModulesTime");

    // Now get the one fro prom-api, which is scaled to seconds
    String response =
    given()
        .header("Accept","text/plain")
    .when()
        .get("http://localhost:8080/metrics/vendor/mscLoadedModulesTime")
        .asString();

    String[] lines = response.split("\n");

    // Find the line and see if this was correctly scaled
    // Entry data is in milliseconds and prom exports as seconds
    // so we need to divide by 1000
    for (String line : lines) {
      if (line.startsWith("vendor:msc_loaded")) {
        String[] items = line.split(" ");
        double theVal = Double.valueOf(items[1]);
        assert theVal == val / 1000d;
        return;
      }
    }
    throw new IllegalStateException("Should have found an entry");
  }
}
