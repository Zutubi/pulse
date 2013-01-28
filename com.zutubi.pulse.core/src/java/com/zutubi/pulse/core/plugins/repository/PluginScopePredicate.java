package com.zutubi.pulse.core.plugins.repository;

import com.google.common.base.Predicate;

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

    public boolean apply(PluginInfo pluginInfo)
    {
        return pluginInfo.getScope().ordinal() <= scope.ordinal();
    }
}
