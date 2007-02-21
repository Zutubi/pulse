package com.zutubi.pulse.plugins;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

import java.net.URL;
import java.util.List;

/**
 */
public interface PluginManager
{
    IExtensionRegistry getExtenstionRegistry();
    IExtensionTracker getExtenstionTracker();

    List<Plugin> getAllPlugins();
    List<PluginRequirement> getRequiredPlugins(Plugin plugin);
    List<Plugin> getDependentPlugins(Plugin plugin);
    Plugin getPlugin(String id);

    /**
     * Installs a plugin from the given URL, using the given filename as the
     * name of the installed plugin file.  A plugin can only be installed if
     * all of its dependencies may be resolved.  If a dependency is not
     * resolvable, the plugin installation fails and the state is unchanged.
     *
     * @param filename
     * @param url
     * @return
     * @throws PluginException
     */
    Plugin installPlugin(String filename, URL url) throws PluginException;
    Plugin installPlugin(URL url) throws PluginException;
    void uninstallPlugin(Plugin plugin) throws PluginException;
    void enablePlugin(Plugin plugin) throws PluginException;
    void disablePlugin(Plugin plugin) throws PluginException;

    Plugin getPlugin(IExtension extension);
}
