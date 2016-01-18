package com.zutubi.pulse.master.rest.model.plugins;

import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginDependency;
import com.zutubi.pulse.core.plugins.PluginVersion;

/**
 * Models a reference to another a plugin.
 */
public class PluginHandleModel
{
    private String id;
    private PluginVersion version;
    private String name;
    private boolean available;

    public PluginHandleModel(PluginDependency pluginDependency)
    {
        id = pluginDependency.getId();
        version = pluginDependency.getVersion();
        available = pluginDependency.getSupplier() != null;
        if (available)
        {
            name = pluginDependency.getSupplier().getName();
        }
    }

    public PluginHandleModel(Plugin plugin)
    {
        id = plugin.getId();
        name = plugin.getName();
        version = plugin.getVersion();
        available = true;
    }

    public String getId()
    {
        return id;
    }

    public String getVersion()
    {
        return version.toString();
    }

    public String getName()
    {
        return name;
    }

    public boolean isAvailable()
    {
        return available;
    }
}
