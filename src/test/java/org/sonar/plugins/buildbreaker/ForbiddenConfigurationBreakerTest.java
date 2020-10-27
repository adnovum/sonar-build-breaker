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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class ForbiddenConfigurationBreakerTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testShouldExecuteSuccess() {
    TestConfiguration config = new TestConfiguration();
    config.setProperty(BuildBreakerPlugin.FORBIDDEN_CONF_KEY, "foo=bar,hello=world");

    assertEquals(true, new ForbiddenConfigurationBreaker(config).shouldExecuteOnProject());
  }

  @Test
  public void testShouldExecuteFailure() {
    TestConfiguration config = new TestConfiguration();

    assertEquals(false, new ForbiddenConfigurationBreaker(config).shouldExecuteOnProject());
  }

  @Test
  public void shouldNotFailWithoutAnyForbiddenConfSet() {
    TestConfiguration config = new TestConfiguration();

    new ForbiddenConfigurationBreaker(config).shouldExecuteOnProject();
    // no exception expected
  }

  @Test
  public void shouldFailIfForbiddenPropertyIsSet() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("A forbidden configuration has been found on the project: foo=bar");

    TestConfiguration config = new TestConfiguration();
    config.setProperty(BuildBreakerPlugin.FORBIDDEN_CONF_KEY, "foo=bar,hello=world");
    config.setProperty("foo", "bar");

    new ForbiddenConfigurationBreaker(config).execute(null);
  }

  @Test
  public void shouldNotFailIfForbiddenPropertyValueIsDifferent() {
    TestConfiguration config = new TestConfiguration();
    config.setProperty(BuildBreakerPlugin.FORBIDDEN_CONF_KEY, "foo=bar,hello=world");
    config.setProperty("foo", "other_value");

    new ForbiddenConfigurationBreaker(config).execute(null);
    // no exception expected
  }

  @Test
  public void shouldFailIfForbiddenBooleanPropertyIsSet() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("A forbidden configuration has been found on the project: foo=true");

    TestConfiguration config = new TestConfiguration();
    config.setProperty(BuildBreakerPlugin.FORBIDDEN_CONF_KEY, "foo=true");
    config.setProperty("foo", "true");

    new ForbiddenConfigurationBreaker(config).execute(null);
  }
}
