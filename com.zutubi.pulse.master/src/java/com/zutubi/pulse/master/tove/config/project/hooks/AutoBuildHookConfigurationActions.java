package com.zutubi.pulse.master.tove.config.project.hooks;

import java.util.List;

/**
 * Actions for automatic build hooks.
 */
public class AutoBuildHookConfigurationActions extends BuildHookConfigurationActions
{
    private static final String ACTION_DISABLE = "disable";
    private static final String ACTION_ENABLE  = "enable";

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
        setInstanceEnabled(instance, true);
    }

    public void doDisable(AutoBuildHookConfiguration instance)
    {
        setInstanceEnabled(instance, false);
    }

    private void setInstanceEnabled(AutoBuildHookConfiguration instance, boolean enabled)
    {
        instance = configurationProvider.deepClone(instance);
        instance.setEnabled(enabled);
        configurationProvider.save(instance);
    }
}
