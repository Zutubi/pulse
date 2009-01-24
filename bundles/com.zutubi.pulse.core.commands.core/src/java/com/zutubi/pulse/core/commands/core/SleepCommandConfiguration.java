package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.BuildingCommandConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 */
@SymbolicName("zutubi.sleepCommandConfig")
public class SleepCommandConfiguration extends BuildingCommandConfigurationSupport
{
    private long interval;

    public SleepCommandConfiguration()
    {
        super(SleepCommand.class);
    }

    public long getInterval()
    {
        return interval;
    }

    public void setInterval(long interval)
    {
        this.interval = interval;
    }
}
