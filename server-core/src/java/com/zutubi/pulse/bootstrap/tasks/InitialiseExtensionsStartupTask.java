package com.zutubi.pulse.bootstrap.tasks;

import com.zutubi.pulse.plugins.PluginManager;
import com.zutubi.pulse.bootstrap.StartupTask;
import com.zutubi.pulse.bootstrap.ComponentContext;

/**
 * A startup task that instructs the plugin manager to initialise extension
 * points.
 */
public class InitialiseExtensionsStartupTask implements StartupTask
{
    public void execute()
    {
        PluginManager pluginManager = (PluginManager) ComponentContext.getBean("pluginManager");
        pluginManager.initialiseExtensions();
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
