package com.zutubi.pulse.prototype.config.admin;

/**
 *
 *
 */
public class LoggingConfiguration
{
    private String level;
    private boolean eventLoggingEnabled;

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
