package com.zutubi.pulse.prototype.config.project.hooks;

import com.zutubi.prototype.config.ConfigurationProvider;

import java.util.List;
import java.util.LinkedList;

/**
 * Actions for automatic build hooks.
 */
public class AutoBuildHookConfigurationActions extends BuildHookConfigurationActions
{
    private static final String ACTION_DISABLE = "disable";
    private static final String ACTION_ENABLE  = "enable";

    private ConfigurationProvider configurationProvider;

    public List<String> getActions(AutoBuildHookConfiguration instance)
    {
        List<String> actions = super.getActions(instance);

        if(instance.isEnabled())
        {
            actions.add(ACTION_DISABLE);
        }
        else
        {
            actions.add(ACTION_ENABLE);
        }

        return actions;
    }

    public void doEnable(AutoBuildHookConfiguration instance)
    {
        instance.setEnabled(true);
        configurationProvider.save(instance);
    }

    public void doDisable(AutoBuildHookConfiguration instance)
    {
        instance.setEnabled(false);
        configurationProvider.save(instance);
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
