package com.zutubi.plugins;

import java.util.List;

/**
 * <class-comment/>
 */
public interface PluginRepository
{
    List<String> listPlugins();

    Plugin getPlugin(String key);

    boolean containsPlugin(Plugin plugin);

    Plugin installPlugin(Plugin plugin);

    void uninstallPlugin(Plugin plugin);

    boolean supportsInstall();

    boolean supportsUninstall();

    /**
     * When the plugin repository is no longer needed, this method should be called
     * to give the repostory an opportunity to close any open resources.
     */
    void destory();
}
