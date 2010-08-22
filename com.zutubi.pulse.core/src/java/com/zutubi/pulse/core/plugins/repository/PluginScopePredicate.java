package com.zutubi.pulse.core.plugins.repository;

import com.zutubi.util.Predicate;

/**
 * A predicate to test if plugins are in a given scope.  Note that a plugin is
 * considered part of its specified scope or any higher scope.
 */
public class PluginScopePredicate implements Predicate<PluginInfo>
{
    private PluginRepository.Scope scope;

    public PluginScopePredicate(PluginRepository.Scope scope)
    {
        this.scope = scope;
    }

    public boolean satisfied(PluginInfo pluginInfo)
    {
        return pluginInfo.getScope().ordinal() <= scope.ordinal();
    }
}
