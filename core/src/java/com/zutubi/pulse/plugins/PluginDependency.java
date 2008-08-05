package com.zutubi.pulse.plugins;

/**
 * Stores information about a dependency requirement of a plugin on another
 * plugin.
 */
public class PluginDependency
{
    private String id;
    private VersionRange versionRange;
    private Plugin supplier;

    public PluginDependency(String id, VersionRange versionRange, Plugin supplier)
    {
        this.id = id;
        this.versionRange = versionRange;
        this.supplier = supplier;
    }

    public String getId()
    {
        return id;
    }

    public VersionRange getVersionRange()
    {
        return versionRange;
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
