package com.zutubi.pulse.plugins;

/**
 * Metadata that describes a plugin and its current state.
 */
public interface Plugin
{
    public enum State
    {
        /**
         * The plugin is currently disabled: it is not loaded or available
         * for use, but remains installed.
         */
        DISABLED,
        /**
         * The plugin could not be loaded.  See the errorMessage field for
         * details.
         */
        ERROR,
        /**
         * The plugin is loaded and available for use.
         */
        ENABLED
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
     * @return the plugin version, which is the OSGi bundle version.  The
     * format is a dotted-decimal style string (e.g. 2.0.0).
     */
    String getDescription();

    /**
     * @return an optional description for the plugin, displayed to users.
     * Matches the OSGi Bundle-Description.
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
     * when state is ERROR.
     */
    String getErrorMessage();

    boolean isEnabled();

    boolean isDisabled();
}
