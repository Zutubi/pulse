package com.zutubi.pulse.core.plugins;

import java.net.URI;
import java.util.List;

/**
 * Metadata that describes a plugin and its current state.
 */
public interface Plugin
{
    /**
     * The state of the plugin within its lifecycle. 
     */
    public enum State
    {
        /**
         * The plugin is loaded and available for use.
         */
        ENABLED,

        /**
         * The plugin is currently disabled: it is not loaded or available for use, but
         * remains on disk.
         */
        DISABLED,

        /**
         * Loading the plugin failed for some reason, explained in the error
         * message where possible.
         */
        ERROR,

        /**
         * The plugin is available but will not be installed until the next
         * restart.
         */
        INSTALLING,
        
        /**
         * The plugin will be uninstalled on restart, and is no longer valid.
         */
        UNINSTALLING,

        /**
         * The plugin will be disabled on restart, and is no longer valid.
         */
        DISABLING,

        /**
         * A new version of the plugin is ready to install, the current
         * version should be removed.
         */
        UPGRADING
    }

    /**
     * @return the plugin id, which is the OSGi bundle symbolic name.  This
     *         is unique for every plugin, and by convention is in the style of a
     *         Java package name (e.g. com.zutubi.pulse.core).
     */
    String getId();

    /**
     * @return the name of the plugin, for display to users
     */
    String getName();

    /**
     * @return the plugin version, which is the OSGi bundle version.  The
     *         format is a dotted-decimal style string (e.g. 2.0.0).
     */
    PluginVersion getVersion();

    /**
     * @return the plugin vendor, as defined within the plugins manifest. Matches
     *          the OSGi Bundle-Vendor
     */
    String getVendor();

    /**
     * @return an optional description for the plugin, displayed to users.
     *         Matches the OSGi Bundle-Description.
     */
    String getDescription();

    /**
     * @return the URI defining from where the plugin was loaded.
     */
    URI getSource();

    /**
     * @return the current state of the plugin.
     */
    State getState();

    /**
     * Indicates if this plugin is currently running in the system.  This
     * includes all plugins that are enabled, or have been enabled but are
     * marked to be otherwise on the next restart.
     * 
     * @return true if this plugin is or was enabled since the last restart 
     */
    boolean isRunning();

    /**
     * @return error messages associated with this plugin.
     */
    List<String> getErrorMessages();

    /**
     * Records a new error message against this plugin.
     * 
     * @param message the message to record
     */
    void addErrorMessage(String message);
    
    /**
     * Enable the plugin.  This will start the plugin and make it available to other
     * plugins that may depend on it.
     * <p/>
     * This is available to disabled and disabling plugins.
     *
     * @see State#DISABLED
     * @see State#DISABLING
     *
     * @throws PluginException if there is a problem enabling the plugin.
     */
    void enable() throws PluginException;

    void disable() throws PluginException;

    void uninstall() throws PluginException;

    Plugin upgrade(URI newSource) throws PluginException;

    /**
     * @return the list of plugins that this plugin depends upon.
     */
    List<PluginDependency> getRequiredPlugins();

    /**
     * @return the list of plugins that depend upon this plugin.
     */
    List<Plugin> getDependentPlugins();

    /**
     * Load the specified class packaged within this plugin.
     *
     * @param name  the name of the class
     * @return  the class instance
     * @throws ClassNotFoundException if the named class could not be located.
     */
    Class<?> loadClass(String name) throws ClassNotFoundException;
}
