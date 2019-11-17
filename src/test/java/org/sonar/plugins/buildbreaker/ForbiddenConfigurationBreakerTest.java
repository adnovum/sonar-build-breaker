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
import org.sonar.api.config.Configuration;
import org.sonar.api.config.Settings;
import org.sonar.api.config.internal.ConfigurationBridge;
import org.sonar.api.config.internal.MapSettings;

public final class ForbiddenConfigurationBreakerTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testShouldExecuteSuccess() {
    Settings settings = new MapSettings();
    settings.setProperty(BuildBreakerPlugin.FORBIDDEN_CONF_KEY, "foo=bar,hello=world");
    Configuration config = new ConfigurationBridge(settings);

    assertEquals(true, new ForbiddenConfigurationBreaker(config).shouldExecuteOnProject());
  }

  @Test
  public void testShouldExecuteFailure() {
    Settings settings = new MapSettings();
    Configuration config = new ConfigurationBridge(settings);

    assertEquals(false, new ForbiddenConfigurationBreaker(config).shouldExecuteOnProject());
  }

  @Test
  public void shouldNotFailWithoutAnyForbiddenConfSet() {
    Settings settings = new MapSettings();
    Configuration config = new ConfigurationBridge(settings);

    new ForbiddenConfigurationBreaker(config).shouldExecuteOnProject();
    // no exception expected
  }

  @Test
  public void shouldFailIfForbiddenPropertyIsSet() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("A forbidden configuration has been found on the project: foo=bar");

    Settings settings = new MapSettings();
    settings.setProperty(BuildBreakerPlugin.FORBIDDEN_CONF_KEY, "foo=bar,hello=world");
    settings.setProperty("foo", "bar");
    Configuration config = new ConfigurationBridge(settings);

    new ForbiddenConfigurationBreaker(config).execute(null);
  }

  @Test
  public void shouldNotFailIfForbiddenPropertyValueIsDifferent() {
    Settings settings = new MapSettings();
    settings.setProperty(BuildBreakerPlugin.FORBIDDEN_CONF_KEY, "foo=bar,hello=world");
    settings.setProperty("foo", "other_value");
    Configuration config = new ConfigurationBridge(settings);

    new ForbiddenConfigurationBreaker(config).execute(null);
    // no exception expected
  }

  @Test
  public void shouldFailIfForbiddenBooleanPropertyIsSet() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("A forbidden configuration has been found on the project: foo=true");

    Settings settings = new MapSettings();
    settings.setProperty(BuildBreakerPlugin.FORBIDDEN_CONF_KEY, "foo=true");
    settings.setProperty("foo", true);
    Configuration config = new ConfigurationBridge(settings);

    new ForbiddenConfigurationBreaker(config).execute(null);
  }
}
