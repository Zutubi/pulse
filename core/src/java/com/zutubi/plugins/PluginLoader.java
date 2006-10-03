package com.zutubi.plugins;

import java.util.List;

/**
 * <class-comment/>
 */
public interface PluginLoader
{
    static final String PLUGIN_DESCRIPTOR_NAME = "plugin.xml";

    /**
     * Load all of the plugins available to this plugin loader.
     *
     * @return a collection of loaded plugins.
     */
    List<Plugin> loadPlugins();
}
