/*
 * SonarQube Build Breaker Plugin
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;

import java.util.Arrays;
import java.util.List;

@Properties({
  @Property(key = BuildBreakerPlugin.SKIP_KEY, defaultValue = "false", name = "Build Breaker skip on alert flag",
    description = "If set to true breaks on alerts are disabled. By default breaks on alerts are enabled.", global = true, project = true,
    type = PropertyType.BOOLEAN),
  @Property(key = BuildBreakerPlugin.FORBIDDEN_CONF_KEY,
    name = "Forbidden configuration parameters",
    description = "Comma-separated list of <code>key=value</code> pairs that should break the build.",
    global = true,
    project = false)
})
public class BuildBreakerPlugin extends SonarPlugin {

  public static final String SKIP_KEY = "sonar.buildbreaker.skip";

  public static final String BUILD_BREAKER_LOG_STAMP = "[BUILD BREAKER] ";

  public static final String FORBIDDEN_CONF_KEY = "sonar.buildbreaker.forbiddenConf";

  @Override
  public List getExtensions() {
    return Arrays.asList(ForbiddenConfigurationBreaker.class);
  }
}
