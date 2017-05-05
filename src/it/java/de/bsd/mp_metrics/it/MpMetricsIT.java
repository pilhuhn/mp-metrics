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

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;

import de.bsd.mp_metrics.Main;
import io.restassured.RestAssured;
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

  @Deployment
  public static Archive createDeployment() throws Exception {
    JAXRSArchive deployment = Main.getJaxrsArchive();
    deployment.addAsResource(new ClassLoaderAsset("mapping.yml"), "de/bsd/mp_metrics/mapping.yml");
    return  deployment;
  }

  @Test
  public void testBadSubTree() {
    when().get("http://localhost:8080/metrics/bad-tree")
        .then()
        .statusCode(404);
  }

  @Test
  public void testListsAll() {
    when().get("http://localhost:8080/metrics")
        .then()
        .statusCode(200)
        .and().contentType("application/json");

    Map response = when().get("http://localhost:8080/metrics")
        .as(Map.class);

    assert response.containsKey("base");
    assert response.containsKey("vendor");
    assert response.containsKey("application");
  }


  @Test
  public void testBase() {
    when().get("http://localhost:8080/metrics/base")
        .then()
        .statusCode(200)
        .and().contentType("application/json")
        .and()
        .body(containsString("total-started-thread-count"));
  }

  @Test
  public void testVendor() {
    when().get("http://localhost:8080/metrics/base")
        .then()
        .contentType("application/json")
        .and()
        .body(containsString("total-started-thread-count"));

  }

  // [{"name":"msc-loaded-modules","displayName":"msc-loaded-modules",
  //  "mbean":"jboss.modules:type=ModuleLoader,name=BootModuleLoader-2/LoadedModuleCount",
  //  "description":"Number of loaded modules","type":"gauge","unit":"none"}]
  @Test
  public void testVendorMetadata() {
    RestAssured.options("http://localhost:8080/metrics/vendor")
        .then().statusCode(200)
        .and().contentType("application/json")
        .and().body("[0].name", is("msc-loaded-modules"));
  }

  @Test
  public void testApplicationMetadata() {
    RestAssured.options("http://localhost:8080/metrics/application")
        .then().statusCode(200)
        .and().contentType("application/json")
        .and().body("[0].name", is("demo"))
        .and().body("[0].tags", containsString("app=demo"))
    ;
  }

  @Test
  public void testApplicationsData() {
    when().get("http://localhost:8080/metrics/application")
        .then().statusCode(200)
        .and().contentType("application/json")
        .and()
        .body(containsString("demo"));

  }

  @Test
  public void testApplicationsDataDemo() {
    // Get the counter
    int count = when().get("http://localhost:8080/demo/count")
        .then().statusCode(200)
        .extract().path("demo");


    when().get("http://localhost:8080/metrics/application/demo")
        .then().statusCode(200)
        .and().contentType("application/json")
        .and()
        .body(containsString("\"demo\":"+count));
  }

  @Test
  public void testApplicationsDataDemo2() {

    // Get the counter
    int count = when().get("http://localhost:8080/demo/count")
        .then().statusCode(200)
        .extract().path("demo");

    // Call hello world to bump the counter
    when().get("http://localhost:8080/demo/hello")
        .then().statusCode(200);
    count++;


    // Compare with what we got from the metrics api
    when().get("http://localhost:8080/metrics/application/demo")
        .then().statusCode(200)
        .and().contentType("application/json")
        .and()
        .body(containsString("\"demo\":"+count));
  }

}
