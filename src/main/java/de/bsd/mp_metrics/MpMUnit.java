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
 * Units for the metrics
 * @author hrupp
 */
public enum MpMUnit {
  NONE ("none"),

  BYTE ("byte"),
  KILO_BYTE ("kbyte"),
  MEGA_BYTE ("mbyte"),

  NANOSECONDS("ns"),
  MICROSECONDS("us"),
  MILLISECOND("ms"),
  SECONDS("s"),
  MINUTES("m"),
  HOURS("h"),
  DAYS("d"),

  PERCENT("%")

  ;


  private final String name;

  MpMUnit(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  public static MpMUnit from(String in) {
    EnumSet<MpMUnit> enumSet = EnumSet.allOf(MpMUnit.class);
    for (MpMUnit u : enumSet) {
      if (u.name.equals(in)) {
        return u;
      }
    }
    throw new IllegalArgumentException(in + " is not a valid MpUnit");
  }
}
