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

import org.apache.commons.configuration.Configuration;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;

import org.sonar.api.batch.BuildBreaker;

public class ForbiddenConfigurationBreaker extends BuildBreaker {

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
        fail("[BUILD BREAKER] Forbidden configuration detected: " + pair);
      }
    }
  }
}
