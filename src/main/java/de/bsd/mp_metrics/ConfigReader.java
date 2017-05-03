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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.jboss.logging.Logger;
import org.yaml.snakeyaml.Yaml;

/**
 * @author hrupp
 */
public class ConfigReader {
  Logger log = Logger.getLogger(this.getClass().getName());


  Metadata readConfig(String mappingFile) {
    try {


      File file = new File(mappingFile);
      log.info("Loading mapping file from " + file.getAbsolutePath());
      InputStream configStream = new FileInputStream(file);

      return readConfig(configStream);
    } catch (FileNotFoundException e) {
      log.warn("No configuration found");
    }
    return null;
  }

  Metadata readConfig(InputStream configStream) {

    Yaml yaml = new Yaml();

    Metadata config = yaml.loadAs(configStream,Metadata.class);
    log.info("Loaded config");
    return config;
  }


  public static void main(String... args) {
    ConfigReader cr = new ConfigReader();
    Metadata config = cr.readConfig(args[0]);
    System.out.println(config.getBase());
  }
}
