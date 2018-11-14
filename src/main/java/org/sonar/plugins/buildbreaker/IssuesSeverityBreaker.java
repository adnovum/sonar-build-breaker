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

import com.google.common.base.Strings;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.AnalysisMode;
import org.sonar.api.batch.postjob.PostJob;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.PostJobDescriptor;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;
import org.sonar.api.issue.ProjectIssues;
import org.sonar.api.rule.Severity;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.Locale;

/**
 * Checks the project's issues for the configured severity level or higher. Breaks the build if any
 * issues at those severities are found.
 */
public final class IssuesSeverityBreaker implements PostJob{
  private static final String CLASSNAME = IssuesSeverityBreaker.class.getSimpleName();

  private static final Logger LOGGER = Loggers.get(ForbiddenConfigurationBreaker.class);

  private final AnalysisMode analysisMode;
  private final ProjectIssues projectIssues;
  private final String issuesSeveritySetting;
  private final int issuesSeveritySettingValue;

  private boolean failed = false;

  /**
   * Constructor used to inject dependencies.
   *
   * @param analysisMode the analysis mode
   * @param projectIssues the project's issues
   * @param settings the project settings
   */
  public IssuesSeverityBreaker(
      AnalysisMode analysisMode, ProjectIssues projectIssues, Settings settings) {
    this.analysisMode = analysisMode;
    this.projectIssues = projectIssues;
    issuesSeveritySetting =
        Strings.nullToEmpty(settings.getString(BuildBreakerPlugin.ISSUES_SEVERITY_KEY))
            .toUpperCase(Locale.US);
    issuesSeveritySettingValue = Severity.ALL.indexOf(issuesSeveritySetting);
  }

  public boolean shouldExecuteOnProject() {
    if (analysisMode.isPublish()) {
      LOGGER.debug(
          "{} is disabled ({} == {})",
          CLASSNAME,
          CoreProperties.ANALYSIS_MODE,
          CoreProperties.ANALYSIS_MODE_PUBLISH);
      return false;
    }
    if (issuesSeveritySettingValue < 0) {
      LOGGER.debug(
          "{} is disabled ({} == {})",
          CLASSNAME,
          BuildBreakerPlugin.ISSUES_SEVERITY_KEY,
          issuesSeveritySetting);
      return false;
    }
    return true;
  }

  @Override
  public void describe(PostJobDescriptor descriptor) {
    descriptor.name("Issues Severity Breaker");
  }

  @Override
  public void execute(PostJobContext postJobContext) {
    if (shouldExecuteOnProject()){
      for (Issue issue : projectIssues.issues()) {
        if (Severity.ALL.indexOf(issue.severity()) >= issuesSeveritySettingValue) {
          // only mark failure and fail on PostJobsPhaseHandler.onPostJobsPhase() to ensure other
          // plugins can finish their work, most notably the stash issue reporter plugin
          String failureMessage =
                  "Project contains issues with severity equal to or higher than " + issuesSeveritySetting;
          LOGGER.error("{} {}", BuildBreakerPlugin.LOG_STAMP, failureMessage);
          throw new IllegalStateException(failureMessage);
        }
      }
    }
  }
}
