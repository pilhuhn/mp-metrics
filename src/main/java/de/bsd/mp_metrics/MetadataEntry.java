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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Bean holding the metadata of one single metric
 * @author hrupp
 */
@SuppressWarnings("unused")
public class MetadataEntry {
  private String name;
  private String displayName;
  @JsonIgnore
  private String mbean;
  private String description;
  private MpMType type;
  private MpMUnit unit;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String tags;

  public MetadataEntry() {
  }

  public MetadataEntry(String name, String displayName, String description, MpMType type, MpMUnit unit) {
    this.name = name;
    this.displayName = displayName;
    this.description = description;
    this.type = type;
    this.unit = unit; // .toString();
  }

  public MetadataEntry(String name, String displayName, String description, MpMType type, MpMUnit unit, String tags) {
    this.name = name;
    this.displayName = displayName;
    this.description = description;
    this.type = type;
    this.unit = unit; // .toString();
    this.tags = tags;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDisplayName() {
    if (displayName==null) {
      return name;
    }
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getMbean() {
    return mbean;
  }

  public void setMbean(String mbean) {
    this.mbean = mbean;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getType() {
    return type.toString();
  }

  public void setType(String type) {
    this.type = MpMType.from(type);
  }

  public String getUnit() {
    return unit.toString();
  }

  public void setUnit(String unit) {
    this.unit = MpMUnit.from(unit);
  }

  public String getTags() {
    String globalTags = System.getenv("MP_METRICS_TAGS");
    if (globalTags!=null && !globalTags.isEmpty()) {
      return tags + "," + globalTags;
    } else {
      return tags;
    }
  }

  public void setTags(String tags) {
    this.tags = tags;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MetadataEntry that = (MetadataEntry) o;

    if (!name.equals(that.name)) return false;
    if (mbean != null ? !mbean.equals(that.mbean) : that.mbean != null) return false;
    if (!type.equals(that.type)) return false;
    return unit.equals(that.unit);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (mbean != null ? mbean.hashCode() : 0);
    result = 31 * result + type.hashCode();
    result = 31 * result + unit.hashCode();
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("MetadataEntry{");
    sb.append("name='").append(name).append('\'');
    sb.append(", mbean='").append(mbean).append('\'');
    sb.append(", type='").append(type).append('\'');
    sb.append(", unit='").append(unit).append('\'');
    sb.append(", tags='").append(tags).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
