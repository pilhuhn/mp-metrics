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
 * Units for the metrics.
 *
 * @author hrupp
 */
@SuppressWarnings("unused")
public enum MpMUnit {
  /** Dummy to say that this has no unit */
  NONE ("none"),

  /** A single Bit. Not defined by SI, but by IEC 60027 */
  BIT("bit"),
  /** 1000 {@link #BIT} */
  KILOBIT("kilobit"),
  /** 1000 {@link #KIBIBIT} */
  MEGABIT("megabit"),
  /** 1000 {@link #MEGABIT} */
  GIGABIT("gigabit"),
  /** 1024 {@link #BIT} */
  KIBIBIT("kibibit"),
  /** 1024 {@link #KIBIBIT}  */
  MEBIBIT("mebibit"),
  /** 1024 {@link #MEBIBIT} */
  GIBIBIT("gibibit"), /* 1024 mebibit */

  /** 8 {@link #BIT} */
  BYTE ("byte"),
  /** 1024 {@link #BYTE} */
  KILO_BYTE ("kbyte"), // 1024 bytes
  /** 1024 {@link #KILO_BYTE} */
  MEGA_BYTE ("mbyte"), // 1024 kilo bytes
  /** 1024 {@link #MEGA_BYTE} */
  GIGA_BYTE("gbyte"),

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
