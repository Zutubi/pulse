package com.zutubi.pulse.core.plugins.repository;


import java.net.URI;
import java.util.List;

/**
 * Represents a location from which plugins can be obtained.
 */
public interface PluginRepository
{
    /**
     * Scopes are used to describe where a plugin may be used.  For example,
     * some plugins may only be useful on a Pulse master.
     * <p/>
     * Note that and plugin in a scope can also be used in all later scopes.
     * So, for example, all CORE plugins may also be used in the MASTER scope.
     */
    enum Scope
    {
        /**
         * Core plugins will work with all Pulse packages.
         */
        CORE("com.zutubi.pulse.core"),
        /**
         * These plugins will work on all Pulse servers (agents and master).
         */
        SERVER("com.zutubi.pulse.servercore"),
        /**
         * Such plugins are only used on the master.
         */
        MASTER("com.zutubi.pulse.master");
        
        private String dependencyId;

        Scope(String dependencyId)
        {
            this.dependencyId = dependencyId;
        }

        /**
         * Returns the id of a plugin that can be used to determine if a plugin
         * belongs to this scope or higher.  If a plugin has a dependency on
         * the plugin of this id, then that plugin may not be used in any
         * lower scopes.
         * 
         * @return id of a plugin, a dependency on which establishes membership
         *         in this scope
         */
        public String getDependencyId()
        {
            return dependencyId;
        }
    }

    /**
     * Returns a list of all plugins that are available in the given or lower
     * scopes.
     *  
     * @param scope the scope to limit the search to
     * @return a list of all plugins in this repository that may be used in
     *        the given scope
     * @throws PluginRepositoryException on any error
     */
    List<PluginInfo> getAvailablePlugins(Scope scope) throws PluginRepositoryException;

    /**
     * Returns the location from which the given plugin's file can be obtained.
     * 
     * @param pluginInfo the plugin to get the location of
     * @return a URI pointing to the file for the given plugin
     */
    URI getPluginLocation(PluginInfo pluginInfo);
}
