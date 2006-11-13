package com.zutubi.plugins;

import java.util.List;

/**
 * <class-comment/>
 */
public interface PluginManager
{
    /**
     * Initialise the plugin manager.
     */
    void init();

    /**
     * Shutdown the plugin manager.  This gives the plugin manager an opportunity to close/release any open resources.
     */
    void destroy();

    /**
     * Retrieve a list of all of the plugins currently available via this plugin manager.
     *
     * @return a list of plugins
     */
    List<? extends Plugin> getPlugins();

    Plugin getPlugin(String key);

    /**
     * Enable the requested plugin. An enabled plugin will have its components enabled.
     *
     * @param plugin
     */
    void enable(Plugin plugin);

    /**
     * Disable the requested plugin.  A disabled plugin will have each of its components disabled.
     *
     * @param plugin
     */
    void disable(Plugin plugin);

    // ---( Dynamic plugin support )---

    /**
     * Install the requested plugin. An installed plugin will be available between system restarts and managed by
     * the plugin manager.
     * <p/>
     * A copy of this plugin will be stored in the plugins directory.
     *
     * @param plugin
     */
    void install(Plugin plugin);

    /**
     * Uninstall the requested plugin.
     * <p/>
     * This plugin will be removed from the plugins directory.
     *
     * @param plugin
     */
    void uninstall(Plugin plugin);
}
