package com.zutubi.pulse.plugins;

import java.net.URL;

/**
 * Metadata that describes a plugin and its current state.
 */
public interface Plugin
{
    public enum State
    {
        /**
         * The plugin is currently disabled: it is not loaded or available
         * for use, but remains on disk.
         */
        DISABLED,
        /**
         * The plugin will be disabled on restart, and is no longer valid.
         */
        DISABLING,
        /**
         * The plugin is loaded and available for use.
         */
        ENABLED,
        /**
         * The plugin will be uninstalled on restart, and is no longer valid.
         */
        UNINSTALLING,
        /**
         * A new version of the plugin is ready to install, the current
         * version should be removed.
         */
        UPDATING
    }

    /**
     * @return the plugin id, which is the OSGi bundle symbolic name.  This
     * is unique for every plugin, and by convention is in the style of a
     * Java package name (e.g. com.zutubi.pulse.core).
     */
    String getId();

    /**
     * @return the name of the plugin, for display to users
     */
    String getName();

    /**
     * @return an optional description for the plugin, displayed to users.
     * Matches the OSGi Bundle-Description.
     */
    String getDescription();

    /**
     * @return the plugin version, which is the OSGi bundle version.  The
     * format is a dotted-decimal style string (e.g. 2.0.0).
     */
    String getVersion();

    /**
     * @return an optional vendor of the plugin, displayed to users.  Matches
     * the OSGi Bundle-Vendor.
     */
    String getVendor();

    /**
     * @return the current state of the plugin.
     */
    State getState();

    /**
     * @return the error message associated with this plugin.  Only valid
     * when state is DISABLED.  If DISABLED and this message is non-null, the
     * message indicates a problem with the plugin that caused it to be
     * automatically disabled.
     */
    String getErrorMessage();

    Class loadClass(String type) throws ClassNotFoundException;

    URL getResource(String path);

    boolean isEnabled();
    boolean canEnable();
    boolean isDisabled();
    boolean canDisable();
    boolean isDisabling();
    boolean isUninstalling();
    boolean canUninstall();
    boolean isUpdating();
    boolean canUpdate();
}
