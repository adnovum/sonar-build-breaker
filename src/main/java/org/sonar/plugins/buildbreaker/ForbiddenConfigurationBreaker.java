/*
 * SonarQube Build Breaker Plugin
 * Copyright (C) 2009-2016 Matthew DeTullio and contributors
 * mailto:sonarqube@googlegroups.com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.buildbreaker;

import com.google.common.base.Splitter;
import org.sonar.api.batch.BuildBreaker;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.List;

public class ForbiddenConfigurationBreaker extends BuildBreaker {

  private static final Logger LOG = Loggers.get(ForbiddenConfigurationBreaker.class);

  private final Settings settings;

  public ForbiddenConfigurationBreaker(Settings settings) {
    this.settings = settings;
  }

  @Override
  public void executeOn(Project project, SensorContext context) {
    String[] pairs = settings.getStringArray(BuildBreakerPlugin.FORBIDDEN_CONF_KEY);
    for (String pair : pairs) {
      List<String> split = Splitter.on('=').limit(2).splitToList(pair);
      if (split.isEmpty()) {
        continue;
      }
      String key = split.get(0);
      String value = split.size() > 1 ? split.get(1) : "";
      if (value.equals(settings.getString(key))) {
        LOG.error(BuildBreakerPlugin.BUILD_BREAKER_LOG_STAMP + "Forbidden configuration: " + pair);
        fail("A forbidden configuration has been found on the project: " + pair);
      }
    }
  }
}
