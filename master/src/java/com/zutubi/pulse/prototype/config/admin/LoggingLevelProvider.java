package com.zutubi.pulse.prototype.config.admin;

import com.zutubi.prototype.OptionProvider;
import com.zutubi.prototype.ListOptionProvider;
import com.zutubi.prototype.type.TypeProperty;
import com.zutubi.pulse.logging.LogConfigurationManager;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Returns the available logging configuration levels.
 */
public class LoggingLevelProvider extends ListOptionProvider
{
    private LogConfigurationManager logConfigurationManager;
    
    public List<String> getOptions(String path, TypeProperty property)
    {
        return logConfigurationManager.getAvailableConfigurations();
    }

    public void setLogConfigurationManager(LogConfigurationManager logConfigurationManager)
    {
        this.logConfigurationManager = logConfigurationManager;
    }
}
