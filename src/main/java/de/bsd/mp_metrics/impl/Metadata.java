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

import de.bsd.mp_metrics.ApplicationMetrics;
import de.bsd.mp_metrics.MetadataEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Class holding the metadata for base, vendor and applications
 * @author hrupp
 */
public class Metadata {

  List<MetadataEntry> base;
  List<MetadataEntry> vendor;
  Map<String,List<Map<String,Object>>> integration;
  List<MetadataEntry> application;

  public List<MetadataEntry> getBase() {
    if (base==null) {
      base = new ArrayList<>(1);
    }
    return base;
  }

  public void setBase(List<MetadataEntry> base) {
    this.base = base;
  }

  public List<MetadataEntry> getVendor() {
    if (vendor==null) {
      vendor = new ArrayList<>(1);
    }
    return vendor;
  }

  public void setVendor(List<MetadataEntry> vendor) {
    this.vendor = vendor;
  }

  public Map<String, List<Map<String,Object>>> getIntegration() {
    if (integration==null) {
      integration = new HashMap(1);
    }
    return integration;
  }

  public void setIntegration(Map<String, List<Map<String, Object>>> integration) {
    this.integration = integration;
  }

  public List<MetadataEntry> getApplication() {
    return ApplicationMetrics.getInstance().getMetadataList();
  }

  public List<MetadataEntry> get(String domain) {
    switch (domain) {
      case "base": return getBase();
      case "vendor" : return getVendor();
//      case "integration": return getIntegration();
      case "application" : return getApplication();
    }
    return Collections.emptyList();
  }
}
