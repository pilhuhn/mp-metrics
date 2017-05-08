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

import java.util.EnumSet;

/**
 * The kind of a metric
 * @author hrupp
 */
@SuppressWarnings("unused")
public enum MpMType {
  /**
   * A Counter monotonically in-/decreases its values.
   * An example could be the number of Transactions committed.
    */
  COUNTER("counter"),
  /**
   * A Gauge has values that 'arbitrarily' go up/down at each
   * sampling. An example could be CPU load
   */
  GAUGE("gauge")
  ;


  private String type;

  MpMType(String type) {
    this.type = type;
  }

  public String toString() {
    return type;
  }

  /**
   * Convert the string representation in to an enum
   * @param in the String representation
   * @return the matching Enum
   * @throws IllegalArgumentException if in is not a valid enum value
   */
  public static MpMType from(String in) {
    EnumSet<MpMType> enumSet = EnumSet.allOf(MpMType.class);
    for (MpMType u : enumSet) {
      if (u.type.equals(in)) {
        return u;
      }
    }
    throw new IllegalArgumentException(in + " is not a valid MpType");
  }
}
