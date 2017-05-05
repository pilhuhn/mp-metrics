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
 * @author hrupp
 */
public enum MpMType {
  COUNTER("counter"),
  GAUGE("gauge")
  ;


  private String type;

  MpMType(String type) {
    this.type = type;
  }

  public String toString() {
    return type;
  }

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
