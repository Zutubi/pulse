package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.annotations.Min;

/**
 * Configuration for instances of {@link SleepCommand}.
 */
@SymbolicName("zutubi.sleepCommandConfig")
@Form(fieldOrder = {"name", "interval", "force"})
public class SleepCommandConfiguration extends CommandConfigurationSupport
{
    @Min(0)
    private int interval;

    public SleepCommandConfiguration()
    {
        super(SleepCommand.class);
    }

    public SleepCommandConfiguration(String name)
    {
        this();
        setName(name);
    }

    public int getInterval()
    {
        return interval;
    }

    public void setInterval(int interval)
    {
        this.interval = interval;
    }
}
