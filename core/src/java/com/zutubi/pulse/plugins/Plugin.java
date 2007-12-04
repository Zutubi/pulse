package com.zutubi.pulse.plugins;

import com.zutubi.pulse.plugins.PluginException;
import com.zutubi.pulse.plugins.Version;

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
    Version getVersion();

    String getVendor();

    /**
     * @return an optional description for the plugin, displayed to users.
     *         Matches the OSGi Bundle-Description.
     */
    String getDescription();

    URI getSource();

    /**
     * @return the current state of the plugin.
     */
    State getState();

    Type getType();

    /**
     * @return the error message associated with this plugin.  Only valid
     *         when state is DISABLED.  If DISABLED and this message is non-null, the
     *         message indicates a problem with the plugin that caused it to be
     *         automatically disabled.
     */
    String getErrorMessage();

    void enable() throws PluginException;

    void disable() throws PluginException;

    void disable(String reason) throws PluginException;

    void uninstall() throws PluginException;

    Plugin upgrade(URI newSource) throws PluginException;

    void resolve() throws PluginException;

    List<PluginRequirement> getRequiredPlugins();

    List<Plugin> getDependentPlugins();

    Class<?> loadClass(String name) throws ClassNotFoundException;

}
