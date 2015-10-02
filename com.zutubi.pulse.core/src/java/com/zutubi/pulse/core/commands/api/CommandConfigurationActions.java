package com.zutubi.pulse.core.commands.api;

import com.zutubi.tove.config.ConfigurationProvider;

import java.util.Collections;
import java.util.List;

/**
 * Actions for {@link CommandConfiguration} instances.
 */
public class CommandConfigurationActions
{
    private static final String ACTION_DISABLE = "disable";
    private static final String ACTION_ENABLE  = "enable";

    private ConfigurationProvider configurationProvider;

    public List<String> getActions(CommandConfiguration instance)
    {
        if (instance.isEnabled())
        {
            return Collections.singletonList(ACTION_DISABLE);
        }
        else
        {
            return Collections.singletonList(ACTION_ENABLE);
        }
    }

    public void doDisable(CommandConfiguration instance)
    {
        setInstanceEnabled(instance, false);
    }

    public void doEnable(CommandConfiguration instance)
    {
        setInstanceEnabled(instance, true);
    }

    private void setInstanceEnabled(CommandConfiguration instance, boolean enabled)
    {
        instance = configurationProvider.deepClone(instance);
        instance.setEnabled(enabled);
        configurationProvider.save(instance);
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
