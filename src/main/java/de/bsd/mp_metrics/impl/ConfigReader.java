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
package de.bsd.mp_metrics.impl;

import de.bsd.mp_metrics.MetadataEntry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.parser.ParserException;

/**
 * @author hrupp
 */
public class ConfigReader {
  Logger log = Logger.getLogger(this.getClass().getName());


  public Metadata readConfig(String mappingFile) {
    try {


      File file = new File(mappingFile);
      log.info("Loading mapping file from " + file.getAbsolutePath());
      InputStream configStream = new FileInputStream(file);

      return readConfig(configStream);
    } catch (FileNotFoundException e) {
      log.warn("No configuration found");
    } catch (ParserException pe) {
      log.error(pe);
    }
    return null;
  }

  public Metadata readConfig(InputStream configStream) {

    Yaml yaml = new Yaml();

    Metadata config = yaml.loadAs(configStream,Metadata.class);
    log.info("Loaded config");
    return config;
  }


  public static void main(String... args) {
    if (args.length==0) {
      System.err.println("Please specify a config file");
      System.err.println("Usage: ConfigReader <config file> [<xml export file>]");
      System.exit(1);
    }
    ConfigReader cr = new ConfigReader();
    Metadata config = cr.readConfig(args[0]);
    if (config!=null) {
      System.out.println(config.getBase());
      System.out.println(config.getIntegration());
    }

    if (args.length==2) {
      exportToXml(args[1], config);
    }
  }

  private static void exportToXml(String xmlOutFile, Metadata metadata) {

    DocumentBuilderFactory factory =
        DocumentBuilderFactory.newInstance();

    try {
      File f = new File(xmlOutFile);
      DocumentBuilder builder =
          factory.newDocumentBuilder();
      Document document = builder.newDocument();
      Element root = document.createElement("config");
      document.appendChild(root);
      List<MetadataEntry> entries = metadata.getBase();
      for (MetadataEntry entry : entries) {
        Element elem = document.createElement("metric");
        elem.setAttribute("name",entry.getName());
        elem.setAttribute("multi",String.valueOf(entry.isMulti()));
        elem.setAttribute("unit",entry.getUnit());
        elem.setAttribute("type",entry.getType());
        root.appendChild(elem);

      }
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(document);
      StreamResult result = new StreamResult(f);

      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      transformer.transform(source, result);
    } catch (TransformerException | ParserConfigurationException e) {
      e.printStackTrace();
    }
  }
}
