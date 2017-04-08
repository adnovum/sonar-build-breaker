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

import java.util.Arrays;
import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.rule.Severity;

/** Registers the plugin with SonarQube and defines the available configuration properties. */
public final class BuildBreakerPlugin extends SonarPlugin {

  static final String LOG_STAMP = "[BUILD BREAKER]";

  static final String SKIP_KEY = "sonar.buildbreaker.skip";

  static final String QUERY_MAX_ATTEMPTS_KEY = "sonar.buildbreaker.queryMaxAttempts";

  static final String QUERY_INTERVAL_KEY = "sonar.buildbreaker.queryInterval";

  private static final String TOTAL_WAIT_TIME_DESCRIPTION =
      String.format(
          "Total wait time is <code>%s * %s</code>.",
          BuildBreakerPlugin.QUERY_MAX_ATTEMPTS_KEY, BuildBreakerPlugin.QUERY_INTERVAL_KEY);

  static final String FORBIDDEN_CONF_KEY = "sonar.buildbreaker.forbiddenConf";

  static final String ALTERNATIVE_SERVER_URL_KEY = "sonar.buildbreaker.alternativeServerUrl";

  static final String ISSUES_SEVERITY_KEY = "sonar.buildbreaker.preview.issuesSeverity";

  static final String DISABLED = "Disabled";

  @Override
  public List getExtensions() {
    return Arrays.asList(
        ForbiddenConfigurationBreaker.class,
        IssuesSeverityBreaker.class,
        QualityGateBreaker.class,
        PropertyDefinition.builder(SKIP_KEY)
            .name("Skip quality gate check")
            .description(
                "If set to true, the quality gate is not checked.  By default the build will break "
                    + "if the project does not pass the quality gate.")
            .onQualifiers(Qualifiers.PROJECT)
            .type(PropertyType.BOOLEAN)
            .defaultValue("false")
            .build(),
        PropertyDefinition.builder(QUERY_MAX_ATTEMPTS_KEY)
            .name("API query max attempts")
            .description(
                "The maximum number of queries to the API when waiting for report processing.  The "
                    + "build will break if this is reached.<br/>"
                    + TOTAL_WAIT_TIME_DESCRIPTION)
            .onQualifiers(Qualifiers.PROJECT)
            .type(PropertyType.INTEGER)
            .defaultValue("30")
            .build(),
        PropertyDefinition.builder(QUERY_INTERVAL_KEY)
            .name("API query interval (ms)")
            .description(
                "The interval between queries to the API when waiting for report processing.<br/>"
                    + BuildBreakerPlugin.TOTAL_WAIT_TIME_DESCRIPTION)
            .onQualifiers(Qualifiers.PROJECT)
            .type(PropertyType.INTEGER)
            .defaultValue("10000")
            .build(),
        PropertyDefinition.builder(FORBIDDEN_CONF_KEY)
            .name("Forbidden configuration parameters")
            .description(
                "Comma-separated list of <code>key=value</code> pairs that should break the build.")
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(ALTERNATIVE_SERVER_URL_KEY)
            .name("Alternative server URL")
            .description(
                "URL to use for web service requests. If unset, uses the <code>serverUrl</code> "
                    + "property from <code>${sonar.working.directory}/report-task.txt</code>.")
            .onQualifiers(Qualifiers.PROJECT)
            .build(),
        PropertyDefinition.builder(ISSUES_SEVERITY_KEY)
            .name("Issues severity failure level (preview analysis)")
            .description(
                "Fails the build in preview analysis mode if the severity of issues is equal to or "
                    + "more severe than the selection.")
            .onQualifiers(Qualifiers.PROJECT)
            .type(PropertyType.SINGLE_SELECT_LIST)
            .options(
                DISABLED,
                Severity.INFO,
                Severity.MINOR,
                Severity.MAJOR,
                Severity.CRITICAL,
                Severity.BLOCKER)
            .defaultValue(DISABLED)
            .build());
  }
}
