/*
 * Sonar Build Breaker Plugin
 * Copyright (C) 2009 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.buildbreaker;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.BuildBreaker;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;

public class ForbiddenConfigurationBreaker extends BuildBreaker {

  private static final Logger LOG = LoggerFactory.getLogger(ForbiddenConfigurationBreaker.class);

  private final Settings settings;

  public ForbiddenConfigurationBreaker(Settings settings) {
    this.settings = settings;
  }

  public void executeOn(Project project, SensorContext context) {
    String[] pairs = settings.getStringArray(BuildBreakerPlugin.FORBIDDEN_CONF_KEY);
    for (String pair : pairs) {
      String key = StringUtils.substringBefore(pair, "=");
      String value = StringUtils.substringAfter(pair, "=");
      if (StringUtils.equals(value, settings.getString(key))) {
        LOG.error(BuildBreakerPlugin.BUILD_BREAKER_LOG_STAMP + "Forbidden configuration: " + pair);
        fail("A forbidden configuration has been found on the project: " + pair);
      }
    }
  }
}
