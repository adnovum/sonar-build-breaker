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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.AnalysisMode;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.config.Settings;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.issue.Issue;
//import org.sonar.api.issue.ProjectIssues;
import org.sonar.api.rule.Severity;

import com.google.common.base.Strings;

import java.util.Arrays;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class IssuesSeverityBreakerTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testShouldExecuteSuccess() {
    AnalysisMode analysisMode = mock(AnalysisMode.class);
    when(analysisMode.isPublish()).thenReturn(false);

    Settings settings = new MapSettings();
    settings.setProperty(BuildBreakerPlugin.ISSUES_SEVERITY_KEY, Severity.MAJOR);
    final int issuesSeveritySettingValue = Severity.ALL.indexOf(Severity.MAJOR);

    assertEquals(
        true,
        new IssuesSeverityBreaker().shouldExecuteOnProject(analysisMode, Severity.MAJOR, issuesSeveritySettingValue));
  }

  @Test
  public void testShouldExecuteWrongAnalysisMode() {
    AnalysisMode analysisMode = mock(AnalysisMode.class);
    when(analysisMode.isPublish()).thenReturn(true);

    Settings settings = new MapSettings();
    settings.setProperty(BuildBreakerPlugin.ISSUES_SEVERITY_KEY, Severity.MAJOR);
    final int issuesSeveritySettingValue = Severity.ALL.indexOf(Severity.MAJOR);
    
    assertEquals(
        false,
        new IssuesSeverityBreaker().shouldExecuteOnProject(analysisMode, Severity.MAJOR, issuesSeveritySettingValue));
  }

  @Test
  public void testShouldExecuteDisabledFromSeveritySetting() {
    AnalysisMode analysisMode = mock(AnalysisMode.class);
    when(analysisMode.isPublish()).thenReturn(false);

    Settings settings = new MapSettings();
    settings.setProperty(BuildBreakerPlugin.ISSUES_SEVERITY_KEY, Severity.MAJOR);
    final int issuesSeveritySettingValue = Severity.ALL.indexOf(BuildBreakerPlugin.DISABLED);

    assertEquals(
        false,
        new IssuesSeverityBreaker()
            .shouldExecuteOnProject(analysisMode, Severity.MAJOR, issuesSeveritySettingValue));
  }

  @Test
  public void testSeverityMajorFoundMajor() {
    AnalysisMode analysisMode = mock(AnalysisMode.class);
    when(analysisMode.isPublish()).thenReturn(false);

    PostJobIssue issue1 = mock(PostJobIssue.class);
    when(issue1.severity()).thenReturn(org.sonar.api.batch.rule.Severity.MINOR);
    PostJobIssue issue2 = mock(PostJobIssue.class);
    when(issue2.severity()).thenReturn(org.sonar.api.batch.rule.Severity.MAJOR);

    Settings settings = new MapSettings();
    settings.setProperty(BuildBreakerPlugin.ISSUES_SEVERITY_KEY, Severity.MAJOR);
  
    PostJobContext postJobContext = mock(PostJobContext.class);
    when(postJobContext.issues()).thenReturn(Arrays.asList(issue1, issue2));
    when(postJobContext.settings()).thenReturn(settings);
    when(postJobContext.analysisMode()).thenReturn(analysisMode);
    
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Project contains issues with severity equal to or higher than MAJOR");

    IssuesSeverityBreaker breaker = new IssuesSeverityBreaker();
    breaker.execute(postJobContext);
  }

  @Test
  public void testSeverityMajorFoundCritical() {
    AnalysisMode analysisMode = mock(AnalysisMode.class);
    when(analysisMode.isPublish()).thenReturn(false);

    PostJobIssue issue1 = mock(PostJobIssue.class);
    when(issue1.severity()).thenReturn(org.sonar.api.batch.rule.Severity.MINOR);
    PostJobIssue issue2 = mock(PostJobIssue.class);
    when(issue2.severity()).thenReturn(org.sonar.api.batch.rule.Severity.CRITICAL);

    Settings settings = new MapSettings();
    settings.setProperty(BuildBreakerPlugin.ISSUES_SEVERITY_KEY, Severity.MAJOR);

    PostJobContext postJobContext = mock(PostJobContext.class);
    when(postJobContext.issues()).thenReturn(Arrays.asList(issue1, issue2));
    when(postJobContext.settings()).thenReturn(settings);
    when(postJobContext.analysisMode()).thenReturn(analysisMode);
    
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Project contains issues with severity equal to or higher than MAJOR");

    IssuesSeverityBreaker breaker = new IssuesSeverityBreaker();
    breaker.execute(postJobContext);
  }

  @Test
  public void testSeverityMajorFoundNone() {
    AnalysisMode analysisMode = mock(AnalysisMode.class);
    when(analysisMode.isPublish()).thenReturn(false);

    PostJobIssue issue1 = mock(PostJobIssue.class);
    when(issue1.severity()).thenReturn(org.sonar.api.batch.rule.Severity.INFO);
    PostJobIssue issue2 = mock(PostJobIssue.class);
    when(issue2.severity()).thenReturn(org.sonar.api.batch.rule.Severity.MINOR);

    Settings settings = new MapSettings();
    settings.setProperty(BuildBreakerPlugin.ISSUES_SEVERITY_KEY, Severity.MAJOR);

    PostJobContext postJobContext = mock(PostJobContext.class);
    when(postJobContext.issues()).thenReturn(Arrays.asList(issue1, issue2));
    when(postJobContext.settings()).thenReturn(settings);
    when(postJobContext.analysisMode()).thenReturn(analysisMode);
    
    IssuesSeverityBreaker breaker = new IssuesSeverityBreaker();
    breaker.execute(postJobContext);
  }
}
