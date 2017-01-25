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

import org.sonar.api.CoreProperties;
import org.sonar.api.batch.AnalysisMode;
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

public class BasicIssuesBuildBreaker implements PostJob, PostJobsPhaseHandler {
  private static final String CLASSNAME = BasicIssuesBuildBreaker.class.getSimpleName();

  private static final Logger LOG = Loggers.get(ForbiddenConfigurationBreaker.class);

  private final ProjectIssues projectIssues;
  private final AnalysisMode analysisMode;
  private final String failForIssuesWithSeverity;
  private final int failForIssueSeverityAsInt;
  
  String failureMessage = null;
  
  public BasicIssuesBuildBreaker(AnalysisMode analysisMode, ProjectIssues projectIssues, Settings settings) {
    this.analysisMode = analysisMode;
    this.projectIssues = projectIssues;
    
    failForIssuesWithSeverity = settings.getString(BuildBreakerPlugin.FAIL_FOR_ISSUES_WITH_SEVERITY_KEY);
    failForIssueSeverityAsInt = Severity.ALL.indexOf(failForIssuesWithSeverity.trim().toUpperCase());
    
  }

  @Override
  public void executeOn(Project project, SensorContext context) {
    if (analysisMode.isPublish()) {
      LOG.debug("{} is disabled ({} == {})", CLASSNAME, CoreProperties.ANALYSIS_MODE, CoreProperties.ANALYSIS_MODE_PUBLISH);
      return;
    }
    
    if(failForIssueSeverityAsInt < 0) {
      LOG.debug("failForIssuesWithSeverity is configured to {}", failForIssuesWithSeverity);
      return;
    }

    int issueCountToFailFor = 0;
    for(Issue issue: projectIssues.issues()) {
      String severity = issue.severity();
      int issueSeverityAsInt = Severity.ALL.indexOf(String.valueOf(severity).trim().toUpperCase());
      if (issueSeverityAsInt >= failForIssueSeverityAsInt) {
        LOG.info("Recording issue {} that has a severity of '{}'",
            issue.key(), severity);
        issueCountToFailFor++;
      }
    }

    if (issueCountToFailFor > 0) {
      String msg = "Project " + project.getName() + " has " + issueCountToFailFor
          + " issues that are of severity equal or higher than " + failForIssuesWithSeverity;
      LOG.debug(msg);
      // only mark failure and fail on PostJobsPhaseHandler.onPostJobsPhase() to ensure other plugins can finish their work, most notably the stash issue reporter plugin
      markFailure(msg);
      
    } else {
      LOG.info("Project " + project.getName() + " has no issues with severity equal or higher than " + failForIssuesWithSeverity);
    }
    
  }
  
  public void markFailure(String failureMessage) {
    this.failureMessage = failureMessage;
  }

  @Override
  public void onPostJobsPhase(PostJobsPhaseEvent event) {
    if(event.isEnd() && failureMessage!=null) {
      LOG.error(BuildBreakerPlugin.BUILD_BREAKER_LOG_STAMP+" "+failureMessage);
      throw new IllegalStateException(failureMessage);
    }
  }

}