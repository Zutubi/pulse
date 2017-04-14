/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.scm.polling;

import com.google.common.base.Predicate;
import com.zutubi.pulse.core.scm.config.api.Pollable;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.Constants;
import com.zutubi.util.time.Clock;
import com.zutubi.util.time.SystemClock;

/**
 * A predicate that is satisfied if the project is ready to be polled
 * according to the scm polling interval and the last time the project
 * was polled.
 *
 * This only works for pollable projects.
 */
public class IsReadyToPollPredicate implements Predicate<Project>
{
    private ConfigurationProvider configurationProvider;
    private Clock clock = new SystemClock();

    public boolean apply(Project project)
    {
        Pollable scm = (Pollable) project.getConfig().getScm();

        if (project.getLastPollTime() != null)
        {
            int pollingInterval = getDefaultPollingInterval();
            if (scm.isCustomPollingInterval())
            {
                pollingInterval = scm.getPollingInterval();
            }

            long lastPollTime = project.getLastPollTime();
            long nextPollTime = lastPollTime + Constants.MINUTE * pollingInterval;

            if (clock.getCurrentTimeMillis() < nextPollTime)
            {
                return false;
            }
        }
        return true;
    }

    public int getDefaultPollingInterval()
    {
        return configurationProvider.get(GlobalConfiguration.class).getScmPollingInterval();
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setClock(Clock clock)
    {
        this.clock = clock;
    }

    @Override
    public String toString()
    {
        return "IsReadyToPoll";
    }
}
