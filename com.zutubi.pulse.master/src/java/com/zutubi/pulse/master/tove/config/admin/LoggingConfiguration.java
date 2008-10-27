package com.zutubi.pulse.master.tove.config.admin;

import com.zutubi.tove.annotations.Classification;
import com.zutubi.tove.annotations.Select;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.AbstractConfiguration;

/**
 */
@SymbolicName("zutubi.loggingConfig")
@Classification(single = "logging")
public class LoggingConfiguration extends AbstractConfiguration
{
    private String level = "default";
    private boolean eventLoggingEnabled = false;

    public LoggingConfiguration()
    {
        setPermanent(true);
    }

    @Select(optionProvider = "LoggingLevelProvider")
    public String getLevel()
    {
        return level;
    }

    public void setLevel(String level)
    {
        this.level = level;
    }

    public boolean isEventLoggingEnabled()
    {
        return eventLoggingEnabled;
    }

    public void setEventLoggingEnabled(boolean eventLoggingEnabled)
    {
        this.eventLoggingEnabled = eventLoggingEnabled;
    }
}
