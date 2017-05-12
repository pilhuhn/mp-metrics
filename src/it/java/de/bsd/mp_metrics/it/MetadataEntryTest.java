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
package de.bsd.mp_metrics.it;

import de.bsd.mp_metrics.MetadataEntry;
import de.bsd.mp_metrics.MpMType;
import de.bsd.mp_metrics.MpMUnit;
import de.bsd.mp_metrics.Tag;
import org.junit.Test;

/**
 * @author hrupp
 */
public class MetadataEntryTest {

  @Test
  public void parseTags() {
    MetadataEntry me = new MetadataEntry("dummy",MpMType.COUNTER, MpMUnit.BIT);
    me.addTags("a=b,c=d");

    assert me.getTags().size()==3; // There is also the one from the env
    verifyTags(me);
  }

  @Test
  public void parseTagsHoles() {
    MetadataEntry me = new MetadataEntry("dummy",MpMType.COUNTER, MpMUnit.BIT);
    me.addTags("a =b,c = d ");

    assert me.getTags().size()==3; // There is also the one from the env
    verifyTags(me);
  }

  private void verifyTags(MetadataEntry me) {
    int i = 0;
    for (Tag t : me.getTags()) {
      if (t.getKey().equals("a") && t.getValue().equals("b")) {
        i++;
      }

      if (t.getKey().equals("c") && t.getValue().equals("d")) {
        i++;
      }
      // From environment
      if (t.getKey().equals("tier") && t.getValue().equals("integration")) {
        i++;
      }
    }
    assert i==3;
  }
}
