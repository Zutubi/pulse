package com.zutubi.pulse.core.plugins;

import com.zutubi.util.Predicate;

/**
 * A predicate satisfied by plugins that are running.
 * 
 * @see com.zutubi.pulse.core.plugins.Plugin#isRunning() 
 */
public class PluginRunningPredicate implements Predicate<Plugin>
{
    public boolean satisfied(Plugin plugin)
    {
        return plugin.isRunning();
    }
}
