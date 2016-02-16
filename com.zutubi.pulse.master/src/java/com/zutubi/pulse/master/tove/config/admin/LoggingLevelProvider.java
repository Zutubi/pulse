package com.zutubi.pulse.master.tove.config.admin;

import com.zutubi.pulse.servercore.util.logging.LogConfigurationManager;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.ui.forms.FormContext;
import com.zutubi.tove.ui.forms.ListOptionProvider;

import java.util.List;

/**
 * Returns the available logging configuration levels.
 */
public class LoggingLevelProvider extends ListOptionProvider
{
    private LogConfigurationManager logConfigurationManager;

    public String getEmptyOption(TypeProperty property, FormContext context)
    {
        // Not templatable.
        return null;
    }

    public List<String> getOptions(TypeProperty property, FormContext context)
    {
        return logConfigurationManager.getAvailableConfigurations();
    }

    public void setLogConfigurationManager(LogConfigurationManager logConfigurationManager)
    {
        this.logConfigurationManager = logConfigurationManager;
    }
}
