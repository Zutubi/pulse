package com.zutubi.pulse.master.tove.config.admin;

import com.zutubi.pulse.servercore.logging.LogConfigurationManager;
import com.zutubi.tove.ListOptionProvider;
import com.zutubi.tove.type.TypeProperty;

import java.util.List;

/**
 * Returns the available logging configuration levels.
 */
public class LoggingLevelProvider extends ListOptionProvider
{
    private LogConfigurationManager logConfigurationManager;

    public String getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        // Not templatable.
        return null;
    }

    public List<String> getOptions(Object instance, String path, TypeProperty property)
    {
        return logConfigurationManager.getAvailableConfigurations();
    }

    public void setLogConfigurationManager(LogConfigurationManager logConfigurationManager)
    {
        this.logConfigurationManager = logConfigurationManager;
    }
}
