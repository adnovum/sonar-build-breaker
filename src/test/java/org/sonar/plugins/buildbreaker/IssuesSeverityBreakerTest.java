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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.AnalysisMode;
import org.sonar.api.batch.events.PostJobsPhaseHandler.PostJobsPhaseEvent;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issue;
import org.sonar.api.issue.ProjectIssues;
import org.sonar.api.rule.Severity;

public final class IssuesSeverityBreakerTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testShouldExecuteSuccess() {
    AnalysisMode analysisMode = mock(AnalysisMode.class);
    when(analysisMode.isPublish()).thenReturn(false);

    ProjectIssues projectIssues = mock(ProjectIssues.class);
    Settings settings = new Settings();
    settings.setProperty(BuildBreakerPlugin.ISSUES_SEVERITY_KEY, Severity.MAJOR);

    // No exception

    assertEquals(
        true,
        new IssuesSeverityBreaker(analysisMode, projectIssues, settings)
            .shouldExecuteOnProject(null));
  }

  @Test
  public void testShouldExecuteWrongAnalysisMode() {
    AnalysisMode analysisMode = mock(AnalysisMode.class);
    when(analysisMode.isPublish()).thenReturn(true);

    ProjectIssues projectIssues = mock(ProjectIssues.class);
    Settings settings = new Settings();
    settings.setProperty(BuildBreakerPlugin.ISSUES_SEVERITY_KEY, Severity.MAJOR);

    // No exception

    assertEquals(
        false,
        new IssuesSeverityBreaker(analysisMode, projectIssues, settings)
            .shouldExecuteOnProject(null));
  }

  @Test
  public void testShouldExecuteDisabledFromSeveritySetting() {
    AnalysisMode analysisMode = mock(AnalysisMode.class);
    when(analysisMode.isPublish()).thenReturn(false);

    ProjectIssues projectIssues = mock(ProjectIssues.class);
    Settings settings = new Settings();
    settings.setProperty(BuildBreakerPlugin.ISSUES_SEVERITY_KEY, BuildBreakerPlugin.DISABLED);

    // No exception

    assertEquals(
        false,
        new IssuesSeverityBreaker(analysisMode, projectIssues, settings)
            .shouldExecuteOnProject(null));
  }

  @Test
  public void testSeverityMajorFoundMajor() {
    Issue issue1 = mock(Issue.class);
    when(issue1.severity()).thenReturn(Severity.MINOR);
    Issue issue2 = mock(Issue.class);
    when(issue2.severity()).thenReturn(Severity.MAJOR);

    ProjectIssues projectIssues = mock(ProjectIssues.class);
    when(projectIssues.issues()).thenReturn(Arrays.asList(issue1, issue2));

    Settings settings = new Settings();
    settings.setProperty(BuildBreakerPlugin.ISSUES_SEVERITY_KEY, Severity.MAJOR);

    IssuesSeverityBreaker breaker = new IssuesSeverityBreaker(null, projectIssues, settings);
    breaker.executeOn(null, null);

    PostJobsPhaseEvent postJobsPhaseEvent = mock(PostJobsPhaseEvent.class);
    when(postJobsPhaseEvent.isEnd()).thenReturn(true);

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Project contains issues with severity equal to or higher than MAJOR");

    breaker.onPostJobsPhase(postJobsPhaseEvent);
  }

  @Test
  public void testSeverityMajorFoundCritical() {
    Issue issue1 = mock(Issue.class);
    when(issue1.severity()).thenReturn(Severity.MINOR);
    Issue issue2 = mock(Issue.class);
    when(issue2.severity()).thenReturn(Severity.CRITICAL);

    ProjectIssues projectIssues = mock(ProjectIssues.class);
    when(projectIssues.issues()).thenReturn(Arrays.asList(issue1, issue2));

    Settings settings = new Settings();
    settings.setProperty(BuildBreakerPlugin.ISSUES_SEVERITY_KEY, Severity.MAJOR);

    IssuesSeverityBreaker breaker = new IssuesSeverityBreaker(null, projectIssues, settings);
    breaker.executeOn(null, null);

    PostJobsPhaseEvent postJobsPhaseEvent = mock(PostJobsPhaseEvent.class);
    when(postJobsPhaseEvent.isEnd()).thenReturn(true);

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Project contains issues with severity equal to or higher than MAJOR");

    breaker.onPostJobsPhase(postJobsPhaseEvent);
  }

  @Test
  public void testSeverityMajorFoundNone() {
    Issue issue1 = mock(Issue.class);
    when(issue1.severity()).thenReturn(Severity.INFO);
    Issue issue2 = mock(Issue.class);
    when(issue2.severity()).thenReturn(Severity.MINOR);

    ProjectIssues projectIssues = mock(ProjectIssues.class);
    when(projectIssues.issues()).thenReturn(Arrays.asList(issue1, issue2));

    Settings settings = new Settings();
    settings.setProperty(BuildBreakerPlugin.ISSUES_SEVERITY_KEY, Severity.MAJOR);

    IssuesSeverityBreaker breaker = new IssuesSeverityBreaker(null, projectIssues, settings);
    breaker.executeOn(null, null);

    PostJobsPhaseEvent postJobsPhaseEvent = mock(PostJobsPhaseEvent.class);
    when(postJobsPhaseEvent.isEnd()).thenReturn(true);

    // No exception

    breaker.onPostJobsPhase(postJobsPhaseEvent);
  }

  @Test
  public void testPhaseNotEnd() {
    ProjectIssues projectIssues = mock(ProjectIssues.class);
    Settings settings = new Settings();

    PostJobsPhaseEvent postJobsPhaseEvent = mock(PostJobsPhaseEvent.class);
    when(postJobsPhaseEvent.isEnd()).thenReturn(false);

    // No exception

    new IssuesSeverityBreaker(null, projectIssues, settings).onPostJobsPhase(postJobsPhaseEvent);
  }
}
