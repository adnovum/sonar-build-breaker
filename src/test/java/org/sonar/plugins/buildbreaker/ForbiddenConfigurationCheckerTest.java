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

import org.junit.Before;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.sonar.api.utils.SonarException;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ForbiddenConfigurationCheckerTest {

  private Configuration conf;
  private ForbiddenConfigurationChecker checker;

  @Before
  public void setUp() {
    conf = mock(Configuration.class);
    when(conf.getString("foo")).thenReturn("bar");
    checker = new ForbiddenConfigurationChecker(conf);
  }

  @Test
  public void shouldNotFailWithoutAnyForbiddenConfSet() {
    checker.executeOn(null, null);
    // no exception expected
  }

  @Test
  public void shouldFail() {
    when(conf.getStringArray(BuildBreakerPlugin.FORBIDDEN_CONF_KEY)).thenReturn(new String[]{ "foo=bar" });
    try {
      checker.executeOn(null, null);
      fail("Sonar exception expected, not supposed to use foo=bar");
    } catch (SonarException sEx) {
      // exception expected
    }
  }

}
