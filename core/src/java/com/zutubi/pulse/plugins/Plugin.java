package com.zutubi.pulse.plugins;

/**
 * Metadata that describes a plugin and its current state.
 */
public class Plugin
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
     * The plugin name, which is the OSGi bundle symbolic name.  This is
     * unique for every plugin, and by convention is in the style of a Java
     * package name (e.g. com.zutubi.pulse.core).
     */
    private String name;
    /**
     * Th plugin version, which is the OSGi bundle version.  The format is a
     * dotted-decimal style string (e.g. 2.0.0).
     */
    private String version;
    /**
     * Optional description for the plugin, displayed to users.  Matches the
     * OSGi Bundle-Description.
     */
    private String description;
    /**
     * Optional vendor of the plugin, displayed to users.  Matches the OSGi
     * Bundle-Vendor.
     */
    private String vendor;
    /**
     * Current state of the plugin.
     */
    private State state;

    /**
     * Creates a plugin with the given name and state.
     *
     * @param name  the plugin name
     * @param state the current state of the plugin
     */
    public Plugin(String name, State state)
    {
        this.name = name;
        this.state = state;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    void setDescription(String description)
    {
        this.description = description;
    }

    public String getVersion()
    {
        return version;
    }

    void setVersion(String version)
    {
        this.version = version;
    }

    public String getVendor()
    {
        return vendor;
    }

    void setVendor(String vendor)
    {
        this.vendor = vendor;
    }

    public State getState()
    {
        return state;
    }

    void setState(State state)
    {
        this.state = state;
    }
}
