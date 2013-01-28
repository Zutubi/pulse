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
}
