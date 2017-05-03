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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

/**
 * @author hrupp
 */
public class AppDataMBean implements DynamicMBean {

  private Map<String,Number> valuesMap = new HashMap<>();

  @Override
  public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
    if (valuesMap.keySet().contains(attribute)) {
      return valuesMap.get(attribute);
    }
    throw new AttributeNotFoundException();
  }

  @Override
  public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
    if (attribute.getValue() instanceof Number) {

      valuesMap.put(attribute.getName(), (Number) attribute.getValue());
    }
    throw new InvalidAttributeValueException("Not a number");
  }

  @Override
  public AttributeList getAttributes(String[] attributes) {
    AttributeList result = new AttributeList();
    for (String att : attributes) {
      if (valuesMap.containsKey(att)) {
        Attribute a = new Attribute(att,valuesMap.get(att));
        result.add(a);
      }
    }
    return result;
  }

  @Override
  public AttributeList setAttributes(AttributeList attributes) {

    for (Attribute att : attributes.asList()) {
      try {
        setAttribute(att);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return attributes;
  }

  @Override
  public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
    throw new MBeanException(new Exception("Invoke not implemented"));
  }

  @Override
  public MBeanInfo getMBeanInfo() {

    MBeanAttributeInfo[] attribs = new MBeanAttributeInfo[valuesMap.size()];
    int i=0;
    for (Map.Entry<String,Number> entry : valuesMap.entrySet()) {
      MBeanAttributeInfo mabi = new MBeanAttributeInfo(entry.getKey(), entry.getValue().getClass().getSimpleName(), entry.getKey(), true, false, true);
      attribs[i] = mabi;
      i++;
    }


    MBeanInfo info = new MBeanInfo("AppDataMBean", "MBean to hold mp_metrics metric data", attribs, null, null, null);

    return info;
  }
}
