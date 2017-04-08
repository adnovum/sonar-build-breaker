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
import org.sonar.api.config.Settings;

public final class ForbiddenConfigurationBreakerTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testShouldExecuteSuccess() {
    Settings settings = new Settings();
    settings.setProperty(BuildBreakerPlugin.FORBIDDEN_CONF_KEY, "foo=bar,hello=world");

    assertEquals(true, new ForbiddenConfigurationBreaker(settings).shouldExecuteOnProject(null));
  }

  @Test
  public void testShouldExecuteFailure() {
    Settings settings = new Settings();

    assertEquals(false, new ForbiddenConfigurationBreaker(settings).shouldExecuteOnProject(null));
  }

  @Test
  public void shouldNotFailWithoutAnyForbiddenConfSet() {
    new ForbiddenConfigurationBreaker(new Settings()).executeOn(null, null);
    // no exception expected
  }

  @Test
  public void shouldFailIfForbiddenPropertyIsSet() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("A forbidden configuration has been found on the project: foo=bar");

    Settings settings = new Settings();
    settings.setProperty(BuildBreakerPlugin.FORBIDDEN_CONF_KEY, "foo=bar,hello=world");
    settings.setProperty("foo", "bar");

    new ForbiddenConfigurationBreaker(settings).executeOn(null, null);
  }

  @Test
  public void shouldNotFailIfForbiddenPropertyValueIsDifferent() {
    Settings settings = new Settings();
    settings.setProperty(BuildBreakerPlugin.FORBIDDEN_CONF_KEY, "foo=bar,hello=world");
    settings.setProperty("foo", "other_value");

    new ForbiddenConfigurationBreaker(settings).executeOn(null, null);
    // no exception expected
  }

  @Test
  public void shouldFailIfForbiddenBooleanPropertyIsSet() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("A forbidden configuration has been found on the project: foo=true");

    Settings settings = new Settings();
    settings.setProperty(BuildBreakerPlugin.FORBIDDEN_CONF_KEY, "foo=true");
    settings.setProperty("foo", true);

    new ForbiddenConfigurationBreaker(settings).executeOn(null, null);
  }
}
