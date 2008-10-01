package com.zutubi.pulse.bootstrap.tasks;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.bootstrap.StartupTask;
import com.zutubi.pulse.plugins.PluginManager;

/**
 * A startup task that instructs the plugin manager to initialise extension
 * points.
 */
public class InitialiseExtensionsStartupTask implements StartupTask
{
    public void execute()
    {
        PluginManager pluginManager = (PluginManager) SpringComponentContext.getBean("pluginManager");
        pluginManager.initialiseExtensions();
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
