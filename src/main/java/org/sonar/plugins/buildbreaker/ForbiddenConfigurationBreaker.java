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

import com.google.common.base.Splitter;
import org.sonar.api.batch.postjob.PostJob;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.PostJobDescriptor;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.List;

/**
 * Checks the analysis parameters for forbidden configurations. Breaks the build if at least one of
 * the comma-separated key=value configurations was found.
 */
public final class ForbiddenConfigurationBreaker implements PostJob {

    private static final Logger LOGGER = Loggers.get(ForbiddenConfigurationBreaker.class);

    private final Settings settings;

    /**
     * Constructor used to inject dependencies.
     *
     * @param settings the project settings
     */
    public ForbiddenConfigurationBreaker(Settings settings) {
        this.settings = settings;
    }

    boolean shouldExecuteOnProject() {
        return settings.hasKey(BuildBreakerPlugin.FORBIDDEN_CONF_KEY);
    }

    @Override
    public void describe(PostJobDescriptor descriptor) {
        descriptor.name("Forbidden Configuration Breaker");
    }

    @Override
    public void execute(PostJobContext context) {
        if (shouldExecuteOnProject()) {
            String[] pairs = settings.getStringArray(BuildBreakerPlugin.FORBIDDEN_CONF_KEY);

            for (String pair : pairs) {
                List<String> split = Splitter.on('=').limit(2).splitToList(pair);
                if (split.isEmpty()) {
                    continue;
                }
                String key = split.get(0);
                String value = split.size() > 1 ? split.get(1) : "";
                if (value.equals(settings.getString(key))) {
                    LOGGER.error("{} Forbidden configuration: {}", BuildBreakerPlugin.LOG_STAMP, pair);
                    throw new IllegalStateException(
                            "A forbidden configuration has been found on the project: " + pair);
                }
            }
        }
    }

}
