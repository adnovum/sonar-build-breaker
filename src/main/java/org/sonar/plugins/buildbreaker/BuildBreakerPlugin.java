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

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.rule.Severity;

import java.util.Arrays;
import java.util.List;

/**
 * Registers the plugin with SonarQube and defines the available configuration properties.
 */
@Properties({
  @Property(key = BuildBreakerPlugin.SKIP_KEY,
    defaultValue = "false",
    name = "Skip quality gate check",
    description = "If set to true, the quality gate is not checked.  By default the build will break if the project does not pass the quality gate.",
    global = true,
    project = true,
    type = PropertyType.BOOLEAN),
  @Property(key = BuildBreakerPlugin.QUERY_MAX_ATTEMPTS_KEY,
    defaultValue = "30",
    name = "API query max attempts",
    description = "The maximum number of queries to the API when waiting for report processing.  The build will break if this is reached." +
      "<br/>" + BuildBreakerPlugin.TOTAL_WAIT_TIME_DESCRIPTION,
    global = true,
    project = true,
    type = PropertyType.INTEGER),
  @Property(key = BuildBreakerPlugin.QUERY_INTERVAL_KEY,
    defaultValue = "10000",
    name = "API query interval (ms)",
    description = "The interval between queries to the API when waiting for report processing." +
      "<br/>" + BuildBreakerPlugin.TOTAL_WAIT_TIME_DESCRIPTION,
    global = true,
    project = true,
    type = PropertyType.INTEGER),
  @Property(key = BuildBreakerPlugin.FORBIDDEN_CONF_KEY,
    name = "Forbidden configuration parameters",
    description = "Comma-separated list of <code>key=value</code> pairs that should break the build.",
    global = true,
    project = false),
  @Property(key = BuildBreakerPlugin.ALTERNATIVE_SERVER_URL_KEY,
    name = "Alternative server URL",
    description = "URL to use for web service requests. If unset, uses the <code>serverUrl</code> property from " +
      "<code>${sonar.working.directory}/report-task.txt</code>.",
    global = true,
    project = false),
  @Property(key = BuildBreakerPlugin.ISSUES_SEVERITY_KEY,
    name = "Issues severity failure level (preview analysis)",
    description = "Fails the build in preview analysis mode if the severity of issues is equal or more severe than the selection.",
    type = PropertyType.SINGLE_SELECT_LIST,
    options = {BuildBreakerPlugin.DISABLED, Severity.INFO, Severity.MINOR, Severity.MAJOR, Severity.CRITICAL, Severity.BLOCKER},
    defaultValue = BuildBreakerPlugin.DISABLED,
    global = true,
    project = true)
})
public class BuildBreakerPlugin extends SonarPlugin {

  public static final String SKIP_KEY = "sonar.buildbreaker.skip";

  public static final String QUERY_MAX_ATTEMPTS_KEY = "sonar.buildbreaker.queryMaxAttempts";

  public static final String QUERY_INTERVAL_KEY = "sonar.buildbreaker.queryInterval";

  public static final String TOTAL_WAIT_TIME_DESCRIPTION = "Total wait time is <code>" +
      BuildBreakerPlugin.QUERY_MAX_ATTEMPTS_KEY + " * " + BuildBreakerPlugin.QUERY_INTERVAL_KEY + "</code>.";

  public static final String BUILD_BREAKER_LOG_STAMP = "[BUILD BREAKER] ";

  public static final String FORBIDDEN_CONF_KEY = "sonar.buildbreaker.forbiddenConf";

  public static final String ALTERNATIVE_SERVER_URL_KEY = "sonar.buildbreaker.alternativeServerUrl";

  public static final String ISSUES_SEVERITY_KEY = "sonar.buildbreaker.preview.issuesSeverity";

  public static final String DISABLED = "Disabled";

  @Override
  public List getExtensions() {
    return Arrays.asList(ForbiddenConfigurationBreaker.class, IssuesSeverityBreaker.class, QualityGateBreaker.class);
  }
}
