package com.zutubi.pulse.master.tove.config.admin;

import com.zutubi.tove.annotations.Classification;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Select;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

/**
 */
@SymbolicName("zutubi.loggingConfig")
@Classification(single = "logging")
@Form(fieldOrder = {"level", "eventLoggingEnabled", "configAuditLoggingEnabled"})
public class LoggingConfiguration extends AbstractConfiguration
{
    private String level = "default";
    private boolean eventLoggingEnabled = false;
    private boolean configAuditLoggingEnabled = false;

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

    public boolean isConfigAuditLoggingEnabled()
    {
        return configAuditLoggingEnabled;
    }

    public void setConfigAuditLoggingEnabled(boolean configAuditLoggingEnabled)
    {
        this.configAuditLoggingEnabled = configAuditLoggingEnabled;
    }
}
