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

import de.bsd.mp_metrics.MpMUnit;
import org.junit.Test;

/**
 * @author hrupp
 */
public class MPUnitsTest {

  @Test
  public void testScaleMinutesToSeconds() {
    MpMUnit foo = MpMUnit.MINUTES;
    double out = MpMUnit.scaleToBase(1, foo);
    assert out == 60;
  }

  @Test
  public void testScaleHoursToSeconds() {
    MpMUnit foo = MpMUnit.HOURS;
    double out = MpMUnit.scaleToBase(3, foo);
    assert out == 3*3600;
  }

  @Test
  public void testScaleMillisecondsToSeconds() {
    MpMUnit foo = MpMUnit.MILLISECOND;
    double out = MpMUnit.scaleToBase(3, foo);
    assert out == 0.003 : "Out was " + out;
  }

  @Test
  public void testScaleNanosecondsToSeconds() {
    MpMUnit foo = MpMUnit.NANOSECONDS;
    double out = MpMUnit.scaleToBase(3, foo);
    assert out == 0.000_003 : "Out was " + out;
  }

  @Test
  public void testScaleMegabyteToByte() {
    MpMUnit foo = MpMUnit.MEGA_BYTE;
    double out = MpMUnit.scaleToBase(1, foo);
    assert out == 1024*1024;
  }

  @Test
  public void testFindBaseUnit1()  {
    MpMUnit foo = MpMUnit.HOURS;
    MpMUnit out = MpMUnit.getBaseUnit(foo);
    assert out.equals(MpMUnit.SECONDS);
    String promUnit = MpMUnit.getBaseUnitAsPrometheusString(out);
    assert promUnit.equals("seconds");
  }

  @Test
  public void testFindBaseUnit2()  {
    MpMUnit foo = MpMUnit.MILLISECOND;
    MpMUnit out = MpMUnit.getBaseUnit(foo);
    assert out.equals(MpMUnit.SECONDS);
    String promUnit = MpMUnit.getBaseUnitAsPrometheusString(out);
    assert promUnit.equals("seconds");
  }

  @Test
  public void testFindBaseUnit3()  {
    MpMUnit foo = MpMUnit.PERCENT;
    MpMUnit out = MpMUnit.getBaseUnit(foo);
    assert out.equals(MpMUnit.PERCENT);
  }

  @Test
  public void testFindBaseUnit4()  {
    MpMUnit foo = MpMUnit.NONE;
    MpMUnit out = MpMUnit.getBaseUnit(foo);
    assert out.equals(MpMUnit.NONE);
  }
}
