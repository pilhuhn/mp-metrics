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
package de.bsd.mp_metrics.demo;

import de.bsd.mp_metrics.ApplicationMetrics;
import de.bsd.mp_metrics.MetadataEntry;
import de.bsd.mp_metrics.MpMType;
import de.bsd.mp_metrics.MpMUnit;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.jboss.logging.Logger;

/**
 * An example application that has a metric registered and is exposing it now.
 * @author hrupp
 */
@Path("/demo")
public class DemoBean {

  private static final String OLA = "ola";
  @Inject
  ApplicationMetrics applicationMetric;

  Logger log = Logger.getLogger(this.getClass().getName());

  // We call this from the JaxRS Application start
  public static void registerMetricsForDemoBean() {
      // Register Metrics for our DemoBean
      ApplicationMetrics applicationMetric = ApplicationMetrics.getInstance();
      MetadataEntry olaEntry = new MetadataEntry(OLA, null, "Just a demo value", MpMType.GAUGE, MpMUnit.NONE);
      olaEntry.setTags("app=ola");
      applicationMetric.registerMetric(olaEntry);
      applicationMetric.storeValue(olaEntry.getName(),42);

      MetadataEntry helloEntry = new MetadataEntry("hello", null, "Just another demo value", MpMType.COUNTER, MpMUnit
          .NONE, "app=shop");
      applicationMetric.registerMetric(helloEntry);

  }

  @GET
  @Path("/hello")
  public String hello() {
    log.info("Hello");
    applicationMetric.bumpValue("hello",1);
    return "Hello World";
  }

  @GET
  @Path("/count")
  @Produces("application/json")
  public String count() {
    Number number = applicationMetric.getValue("hello");
    long value = number.longValue();
    return "{\"hello\":" + value + "}";
  }
}
