package com.zutubi.plugins.internal;

import com.zutubi.plugins.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultPluginManager implements PluginManager
{
    private List<DefaultPlugin> plugins = new LinkedList<DefaultPlugin>();

    private List<PluginLoader> loaders = new LinkedList<PluginLoader>();

    private PluginRepository pluginRepository = null;

    public void setPluginRepository(PluginRepository pluginRepository)
    {
        this.pluginRepository = pluginRepository;
    }

    public void setPluginLoaders(List<PluginLoader> loaders)
    {
        this.loaders = loaders;
    }

    public void addPluginLoader(PluginLoader loader)
    {
        this.loaders.add(loader);
    }

    public void init()
    {

    }

    public void destroy()
    {

    }

    public List<? extends Plugin> getPlugins()
    {
        return Collections.unmodifiableList(plugins);
    }

    public Plugin getPlugin(String key)
    {
        for (Plugin plugin : plugins)
        {
            if (plugin.getKey().equals(key))
            {
                return plugin;
            }
        }
        return null;
    }

    public void enable(Plugin plugin)
    {
        plugin.enable();
    }

    public void disable(Plugin plugin)
    {
        plugin.disable();
    }

    public void install(Plugin plugin)
    {
        pluginRepository.installPlugin(plugin);
    }

    public void uninstall(Plugin plugin)
    {
        pluginRepository.uninstallPlugin(plugin);
    }
}
