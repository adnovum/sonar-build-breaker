/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.buildbreaker;

import org.junit.Test;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;
import org.slf4j.Logger;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.MeasuresFilter;
import org.sonar.api.measures.Metric;
import org.sonar.api.utils.SonarException;

import java.util.Arrays;

public class AlertThresholdCheckerTest {

  @Test
  public void doNotFailWhenNoAlerts() {
    AlertThresholdChecker checker = new AlertThresholdChecker();
    Logger logger = mock(Logger.class);
    SensorContext context = mock(SensorContext.class);
    when(context.getMeasures((MeasuresFilter) anyObject())).thenReturn(Arrays.<Measure>asList(
        newMeasure(CoreMetrics.LINES, null, null),
        newMeasure(CoreMetrics.COVERAGE, Metric.Level.OK, null),
        newMeasure(CoreMetrics.CLASS_COMPLEXITY, Metric.Level.OK, null)
    ));

    checker.analyseMeasures(context, logger);

    verify(logger, never()).error(anyString());
  }
 
  @Test
  public void doNotFailWhenWarningAlerts() {
    AlertThresholdChecker checker = new AlertThresholdChecker();
    Logger logger = mock(Logger.class);
    SensorContext context = mock(SensorContext.class);
    when(context.getMeasures((MeasuresFilter) anyObject())).thenReturn(Arrays.<Measure>asList(
        newMeasure(CoreMetrics.LINES, null, null),
        newMeasure(CoreMetrics.COVERAGE, Metric.Level.WARN, "Coverage<80"),
        newMeasure(CoreMetrics.CLASS_COMPLEXITY, Metric.Level.OK, null)
    ));

    checker.analyseMeasures(context, logger);

    verify(logger, never()).error(anyString());
    verify(logger).warn("Coverage<80");
  }

  @Test
  public void failWhenErrorAlerts() {
    AlertThresholdChecker checker = new AlertThresholdChecker();
    Logger logger = mock(Logger.class);
    SensorContext context = mock(SensorContext.class);
    when(context.getMeasures((MeasuresFilter) anyObject())).thenReturn(Arrays.<Measure>asList(
        newMeasure(CoreMetrics.LINES, null, null),
        newMeasure(CoreMetrics.COVERAGE, Metric.Level.ERROR, "Coverage<80"),
        newMeasure(CoreMetrics.CLASS_COMPLEXITY, Metric.Level.ERROR, "Class complexity>50")
    ));

    try {
      checker.analyseMeasures(context, logger);
      fail();
      
    } catch (SonarException e) {
      verify(logger).error("Coverage<80");
      verify(logger).error("Class complexity>50");
    }
  }

  @Test
  public void doNotCheckGlobalAlertStatus() {
    AlertThresholdChecker checker = new AlertThresholdChecker();
    Logger logger = mock(Logger.class);
    SensorContext context = mock(SensorContext.class);
    when(context.getMeasures((MeasuresFilter) anyObject())).thenReturn(Arrays.<Measure>asList(
        newMeasure(CoreMetrics.COVERAGE, Metric.Level.OK, null),
        newMeasure(CoreMetrics.ALERT_STATUS, Metric.Level.ERROR, "Class complexity>50")
    ));

    checker.analyseMeasures(context, logger);

    verify(logger, never()).error(anyString());
  }

  private Measure newMeasure(Metric metric, Metric.Level level, String label) {
    return new Measure(metric).setAlertStatus(level).setAlertText(label);
  }
}
