package com.zutubi.plugins.repositories;

import com.zutubi.plugins.PluginRepository;
import com.zutubi.plugins.Plugin;

import java.util.List;

/**
 * <class-comment/>
 */
public class RemotePluginRepository implements PluginRepository
{
    public List listPlugins()
    {
        return null;
    }

    public Plugin getPlugin(String key)
    {
        return null;
    }

    public boolean containsPlugin(Plugin plugin)
    {
        return false;
    }

    public Plugin installPlugin(Plugin plugin)
    {
        return null;
    }

    public void uninstallPlugin(Plugin plugin)
    {

    }

    public boolean supportsInstall()
    {
        return false;
    }

    public boolean supportsUninstall()
    {
        return false;
    }

    public void destory()
    {

    }
}
