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
package de.bsd.mp_metrics;

import java.io.File;
import java.util.Arrays;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

/**
 * @author hrupp
 */
public class Main {
  public static void main(String... args) throws Exception {

    System.err.println("ARGS: " + Arrays.toString(args));
    // Instantiate the container
    Swarm swarm = new Swarm(args);
    // Create one or more deployments
    JAXRSArchive deployment = getJaxrsArchive();
//    deployment.addAsResource("mapping.yml"); // TODO figure out how to add the mapping to the archive

//    deployment.as(ZipExporter.class).exportTo(new
//                                                  File("/tmp/archive.war"), true);


    swarm.start();
    swarm.deploy(deployment);
  }

  public static JAXRSArchive getJaxrsArchive() throws Exception {
    JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);

    // For snakeyaml - TODO find out how to use the one from Swarm server
    deployment.addAllDependencies();
    // Add resource to deployment
    deployment.addClass(MpMetricApplication.class);
    deployment.addClass(MpMetricsWorker.class);
    deployment.addClass(ConfigHolder.class);
    deployment.addClass(ConfigReader.class);
    deployment.addClass(Metadata.class);
    deployment.addClass(MetadataEntry.class);

    deployment.addClass(ApplicationMetric.class);
    deployment.addClass(AppDataMBean.class);


    deployment.addClass(DemoBean.class);

    return deployment;
  }
}
