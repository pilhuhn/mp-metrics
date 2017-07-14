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
import java.util.List;

/**
 * @author hrupp
 */
public class ExtMetadata extends Metadata {

  private List<MetadataEntry> integrations;

  public ExtMetadata(Metadata config) {
    super();
    setBase(config.getBase());
    setVendor(config.getVendor());
  }

  public List<MetadataEntry> getIntegrations() {
    return integrations;
  }

  public void setIntegrations(List<MetadataEntry> integrations) {
    this.integrations = integrations;
  }

  @Override
  public List<MetadataEntry> get(String domain) {
    if ("integration".equals(domain)) {
      return getIntegrations();
    }
    else {
      return super.get(domain);
    }
  }
}
