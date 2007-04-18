package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.prototype.annotation.Select;
import com.zutubi.pulse.prototype.record.SymbolicName;

/**
 */
@SymbolicName("loggingConfig")
public class LoggingConfiguration
{
    private String level = "default";
    private boolean eventLoggingEnabled = false;

    @Select(optionProvider = LoggingLevelProvider.class)
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
