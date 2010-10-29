/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.buildbreaker;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class BuildBreakerPluginTest {

  @Test
  public void oneExtensionIsRegistered() {
    assertThat(new BuildBreakerPlugin().getExtensions().size(), is(1));
  }

  @Test
  public void justToIncreaseCoverage() {
    assertThat(new BuildBreakerPlugin().getName(), not(nullValue()));
    assertThat(new BuildBreakerPlugin().getKey(), is("build-breaker"));
    assertThat(new BuildBreakerPlugin().getDescription(), not(nullValue()));
  }
}
