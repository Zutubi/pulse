package com.zutubi.pulse.plugins;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

import java.net.URL;
import java.util.List;

/**
 */
public interface PluginManager
{
    IExtensionRegistry getExtenstionRegistry();
    IExtensionTracker getExtenstionTracker();

    List<? extends Plugin> getAllPlugins();
    Plugin getPlugin(String id);
    Plugin installPlugin(String filename, URL url) throws PluginException;
    Plugin installPlugin(URL url) throws PluginException;
    void uninstallPlugin(Plugin plugin) throws PluginException;
    void enablePlugin(Plugin plugin) throws PluginException;
    void disablePlugin(Plugin plugin) throws PluginException;

    Plugin getPlugin(IExtension extension);
}
