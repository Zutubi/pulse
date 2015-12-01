package com.zutubi.pulse.slave.bootstrap.tasks;

import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;

/**
 * A startup task that instructs the plugin manager to initialise extension
 * points.
 */
public class InitialiseExtensionsStartupTask implements StartupTask
{
    public void execute()
    {
        PluginManager pluginManager = SpringComponentContext.getBean("pluginManager");
        pluginManager.initialiseExtensions();
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
