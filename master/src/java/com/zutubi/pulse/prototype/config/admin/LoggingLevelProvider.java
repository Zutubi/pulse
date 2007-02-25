package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.prototype.OptionProvider;
import com.zutubi.pulse.logging.LogConfigurationManager;

import java.util.List;

/**
 *
 *
 */
public class LoggingLevelProvider implements OptionProvider
{
    private LogConfigurationManager logConfigurationManager;
    
    public List<String> getOptions()
    {
        return logConfigurationManager.getAvailableConfigurations();
    }

    public void setLogConfigurationManager(LogConfigurationManager logConfigurationManager)
    {
        this.logConfigurationManager = logConfigurationManager;
    }
}
