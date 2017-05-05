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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holder for application metrics
 * @author hrupp
 */
public class ApplicationMetrics implements Serializable {

  private Map<String,Number> values = new HashMap<>();
  private Map<String,MetadataEntry> metadata = new HashMap<>();
  private static final ApplicationMetrics theInstance = new ApplicationMetrics();

  private ApplicationMetrics() {
  }

  public static ApplicationMetrics getInstance() {
    return theInstance;
  }

  public void registerMetric(String key, MetadataEntry theData) {
    this.metadata.put(key,theData);
    this.values.put(key,0);

  }

  public void storeValue(String key, Number value) {
    if (!metadata.containsKey(key)) {
      throw new IllegalArgumentException("Unknown metric '" + key + "'");
    }
    values.put(key,value);
  }

  public Number getValue(String key) {
    if (!metadata.containsKey(key)) {
      throw new IllegalArgumentException("Unknown metric '" + key + "'");
    }

    return values.getOrDefault(key, 0);
  }

  public Number bumpValue(String key, int increment) {
    Number num = getValue(key);
    if (num instanceof Float) {
      num = num.floatValue() + increment;
    } else {
      num = num.longValue() + increment;
    }
    storeValue(key,num);
    return num;
  }

  public Number bumpValue(String key) {
    return bumpValue(key,1);
  }




  public Map<String, Number> getAll() {
    return new HashMap<>(values);
  }

  public Map<String,MetadataEntry> getAllMetaData() {
    return new HashMap<>(metadata);
  }


  public List<MetadataEntry> getMetadataList() {
    return new ArrayList<>(metadata.values());
  }
}
