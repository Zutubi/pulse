package com.zutubi.pulse.core.plugins;

import com.zutubi.util.Predicate;

/**
 * A predicate satisfied by plugins with a given id
 */
public class PluginIdPredicate implements Predicate<Plugin>
{
    private String id;

    public PluginIdPredicate(String id)
    {
        this.id = id;
    }

    public boolean satisfied(Plugin plugin)
    {
        return plugin.getId().equals(id);
    }
}
