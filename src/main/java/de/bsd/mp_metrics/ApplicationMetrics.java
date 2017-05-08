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
@SuppressWarnings("unused")
public class ApplicationMetrics implements Serializable {

  private Map<String,Number> values = new HashMap<>();
  private Map<String,MetadataEntry> metadata = new HashMap<>();
  private static final ApplicationMetrics theInstance = new ApplicationMetrics();

  private ApplicationMetrics() {
  }

  public static ApplicationMetrics getInstance() {
    return theInstance;
  }

  /**
   * Register an application metric via its metadata.
   * It is required that each application metric has a unique name
   * set in its metadata.
   * If a metric is registered, but no value has been set yet, it will
   * return 0 - both via REST api and via #getValue
   * @param theData The metadata
   */
  public void registerMetric(MetadataEntry theData) {
    String name = theData.getName();
    if (name ==null || name.isEmpty()) {
      throw new IllegalArgumentException("Name must not be null or empty");
    }

    this.metadata.put(name,theData);
    this.values.put(name,0);

  }

  /**
   * Store a value for key to be exposed by the rest-api
   * @param key the name of a metric
   * @param value the value
   * @throws IllegalArgumentException if the key was not registered.
   */
  public void storeValue(String key, Number value) {
    if (!metadata.containsKey(key)) {
      throw new IllegalArgumentException("Unknown metric '" + key + "'");
    }
    values.put(key,value);
  }

  /**
   * Retrieve the value of the key
   * @param key The name of the metric
   * @throws IllegalArgumentException if the key was not registered.
   * @return a previously set numeric value or 0 otherwise
   */
  public Number getValue(String key) {
    if (!metadata.containsKey(key)) {
      throw new IllegalArgumentException("Unknown metric '" + key + "'");
    }

    return values.getOrDefault(key, 0);
  }

  /**
   * Increase the value of a given metric by a certain delta
   * @param key The name of the metric
   * @param increment increment (could be negative to decrement)
   * @return The new value
   * @throws IllegalArgumentException if the key was not registered.
   */
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

  /**
   * Increase the value of the given metric by 1.
   * @param key Name of the metric
   * @return The new value of the metric
   * @throws IllegalArgumentException if the key was not registered.
   */
  public Number bumpValue(String key) {
    return bumpValue(key,1);
  }


  /**
   * Return a map with the key + numeric values of all registered metrics
   * @return New map with keys and values
   */
  public Map<String, Number> getAll() {
    return new HashMap<>(values);
  }

  /**
   * Return a map with all keys + metadata for all registered metrics
   * @return New map with keys and metadata
   */
  public Map<String,MetadataEntry> getAllMetaData() {
    return new HashMap<>(metadata);
  }


  /**
   * Return a list of metadata items of the registered metrics.
   * @return New list of metadata items
   */
  public List<MetadataEntry> getMetadataList() {
    return new ArrayList<>(metadata.values());
  }
}
