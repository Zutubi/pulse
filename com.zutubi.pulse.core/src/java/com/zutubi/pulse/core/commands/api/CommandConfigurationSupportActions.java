package com.zutubi.pulse.core.commands.api;

import com.zutubi.tove.config.ConfigurationProvider;

import java.util.List;
import java.util.LinkedList;

/**
 * The set of base actions available for all commands that extend
 * the {@link CommandConfigurationSupport} base class.
 */
public class CommandConfigurationSupportActions
{
    public static final String ACTION_ENABLE = "enable";
    public static final String ACTION_DISABLE = "disable";

    private ConfigurationProvider configurationProvider;

    public List<String> getActions(CommandConfiguration instance)
    {
        List<String> actions = new LinkedList<String>();
        actions.add(instance.isEnabled() ? ACTION_DISABLE : ACTION_ENABLE);
        return actions;
    }

    /**
     * Mark the command instance as enabled.
     *
     * @param instance  the command instance to be enabled.
     */
    public void doEnable(CommandConfiguration instance)
    {
        instance.setEnabled(true);
        configurationProvider.save(instance);
    }

    /**
     * Mark the command instance as disabled.
     *
     * @param instance  the command instance to be disabled.
     */
    public void doDisable(CommandConfiguration instance)
    {
        instance.setEnabled(false);
        configurationProvider.save(instance);
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
