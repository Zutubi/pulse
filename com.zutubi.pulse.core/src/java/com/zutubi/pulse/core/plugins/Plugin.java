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
         * An installed plugin has been registered with the plugin registry and is available
         * for startup.  All plugins must be installed before they can be enabled.
         *
         * The installed state differs from the Equinox installed state in that an installed
         * plugin is not necessarily known to equinox. 
         */
        INSTALLED,

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
         * The plugin has been uninstalled, and is no longer available for use.
         *
         * The primary purpose of this state is to remember which plugins have been
         * uninstalled in the past
         */
        UNINSTALLED,

        /**
         * Indicates that there was a plugin version change since the last time the plugin
         * system was initialised and the plugin may require an upgrading.
         */
        VERSION_CHANGE,
        
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
        UPDATING
    }

    public enum Type
    {
        /**
         * Internal plugins are required plugins that are shipped and located within the
         * pulse distribution and can not be uninstalled.
         */
        INTERNAL,
        
        /**
         * User plugins are optional plugins that can be configured by the user.
         */
        USER
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
     * @see com.zutubi.pulse.core.plugins.Plugin.Type
     * @return the type of this plugin.
     */
    Type getType();

    /**
     * @return the error message associated with this plugin.  Only valid
     *         when state is DISABLED.  If DISABLED and this message is non-null, the
     *         message indicates a problem with the plugin that caused it to be
     *         automatically disabled.
     */
    String getErrorMessage();

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

    void disable(String reason) throws PluginException;

    void uninstall() throws PluginException;

    Plugin upgrade(URI newSource) throws PluginException;

    void resolve() throws PluginException;

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
