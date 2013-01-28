package com.zutubi.pulse.core.plugins;

import com.google.common.base.Predicate;

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

    public boolean apply(Plugin plugin)
    {
        return plugin.getId().equals(id);
    }
}
