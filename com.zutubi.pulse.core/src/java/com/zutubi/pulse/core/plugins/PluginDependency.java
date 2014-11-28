package com.zutubi.pulse.core.plugins;

/**
 * Stores information about a dependency requirement of a plugin on another
 * plugin.
 */
public class PluginDependency
{
    private String id;
    private PluginVersion version;
    private Plugin supplier;

    public PluginDependency(String id, PluginVersion version, Plugin supplier)
    {
        this.id = id;
        this.version = version;
        this.supplier = supplier;
    }

    public String getId()
    {
        return id;
    }

    public PluginVersion getVersion()
    {
        return version;
    }

    /**
     * @return the plugin that is supplying this dependency, or null if there
     * is no supplier.  Note that the supplier may not actually be loaded: in
     * which case the dependent plugin may have failed to load.
     */
    public Plugin getSupplier()
    {
        return supplier;
    }
}
