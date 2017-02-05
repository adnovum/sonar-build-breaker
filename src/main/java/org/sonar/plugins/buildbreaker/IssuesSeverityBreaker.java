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
import java.util.Locale;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.AnalysisMode;
import org.sonar.api.batch.CheckProject;
import org.sonar.api.batch.PostJob;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.events.PostJobsPhaseHandler;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;
import org.sonar.api.issue.ProjectIssues;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.Severity;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public final class IssuesSeverityBreaker implements CheckProject, PostJob, PostJobsPhaseHandler {
  private static final String CLASSNAME = IssuesSeverityBreaker.class.getSimpleName();

  private static final Logger LOG = Loggers.get(ForbiddenConfigurationBreaker.class);

  private final AnalysisMode analysisMode;
  private final ProjectIssues projectIssues;
  private final String issuesSeveritySetting;
  private final int issuesSeveritySettingValue;

  private String failureMessage = null;

  public IssuesSeverityBreaker(
      AnalysisMode analysisMode, ProjectIssues projectIssues, Settings settings) {
    this.analysisMode = analysisMode;
    this.projectIssues = projectIssues;
    issuesSeveritySetting =
        Strings.nullToEmpty(settings.getString(BuildBreakerPlugin.ISSUES_SEVERITY_KEY))
            .toUpperCase(Locale.US);
    issuesSeveritySettingValue = Severity.ALL.indexOf(issuesSeveritySetting);
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    if (analysisMode.isPublish()) {
      LOG.debug(
          "{} is disabled ({} == {})",
          CLASSNAME,
          CoreProperties.ANALYSIS_MODE,
          CoreProperties.ANALYSIS_MODE_PUBLISH);
      return false;
    }
    if (issuesSeveritySettingValue < 0) {
      LOG.debug(
          "{} is disabled ({} == {})",
          CLASSNAME,
          BuildBreakerPlugin.ISSUES_SEVERITY_KEY,
          issuesSeveritySetting);
      return false;
    }
    return true;
  }

  @Override
  public void executeOn(Project project, SensorContext context) {
    int issueCountToFailFor = 0;
    for (Issue issue : projectIssues.issues()) {
      if (Severity.ALL.indexOf(issue.severity()) >= issuesSeveritySettingValue) {
        LOG.debug("Recording issue {} that has a severity of '{}'", issue.key(), issue.severity());
        issueCountToFailFor++;
      }
    }

    if (issueCountToFailFor > 0) {
      // only mark failure and fail on PostJobsPhaseHandler.onPostJobsPhase() to ensure other
      // plugins can finish their work, most notably the stash issue reporter plugin
      failureMessage =
          "Found "
              + issueCountToFailFor
              + " issues that are of severity equal or higher than "
              + issuesSeveritySetting;

    } else {
      LOG.info("No issues with severity equal or higher than {}", issuesSeveritySetting);
    }
  }

  @Override
  public void onPostJobsPhase(PostJobsPhaseEvent event) {
    if (event.isEnd() && failureMessage != null) {
      LOG.error("{} {}", BuildBreakerPlugin.LOG_STAMP, failureMessage);
      throw new IllegalStateException(failureMessage);
    }
  }
}
