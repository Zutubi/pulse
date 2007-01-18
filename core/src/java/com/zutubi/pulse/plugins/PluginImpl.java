package com.zutubi.pulse.plugins;

import org.osgi.framework.Bundle;

import java.io.File;

/**
 * Metadata that describes a plugin and its current state.
 */
public class PluginImpl implements Plugin
{
    enum Type
    {
        INTERNAL,
        PREPACKAGED,
        USER
    }

    /**
     * @see com.zutubi.pulse.plugins.Plugin#getId()
     */
    private String id;
    /**
     * @see com.zutubi.pulse.plugins.Plugin#getVersion()
     */
    private String version;
    /**
     * @see com.zutubi.pulse.plugins.Plugin#getName()
     */
    private String name;
    /**
     * @see com.zutubi.pulse.plugins.Plugin#getDescription()
     */
    private String description;
    /**
     * @see com.zutubi.pulse.plugins.Plugin#getVendor()
     */
    private String vendor;
    /**
     * @see com.zutubi.pulse.plugins.Plugin#getState()
     */
    private Plugin.State state;
    /**
     * @see com.zutubi.pulse.plugins.Plugin#getErrorMessage()
     */
    private String errorMessage;
    /**
     * The file the plugin is loaded from.  This may be a single jar file, or
     * a directory.
     */
    private File pluginFile;
    /**
     * The type of the plugin, for example it may be internal to Pulse.
     */
    private Type type;
    /**
     * The OSGi bundle for this plugin.  Only valid in the ENABLED state.
     */
    private Bundle bundle;

    /**
     * Creates a plugin with the given details.
     *
     * @param id    the plugin id
     * @param name  the plugin name
     * @param state the current state of the plugin
     * @param type  the type of the plugin
     */
    public PluginImpl(String id, String name, File pluginFile, PluginImpl.State state, Type type)
    {
        this.id = id;
        this.name = name;
        this.pluginFile = pluginFile;
        this.state = state;
        this.type = type;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    void setName(String name)
    {
        this.name = name;
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

    public Plugin.State getState()
    {
        return state;
    }

    void setState(PluginImpl.State state)
    {
        this.state = state;
        if(state != State.DISABLED)
        {
            errorMessage = null;
        }
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    File getPluginFile()
    {
        return pluginFile;
    }

    void setPluginFile(File pluginFile)
    {
        this.pluginFile = pluginFile;
    }

    boolean isInternal()
    {
        return type == Type.INTERNAL;
    }

    Type getType()
    {
        return type;
    }

    void setType(Type type)
    {
        this.type = type;
    }

    Bundle getBundle()
    {
        return bundle;
    }

    void setBundle(Bundle bundle)
    {
        this.bundle = bundle;
    }

    public boolean isEnabled()
    {
        return state == State.ENABLED;
    }

    public boolean isDisabled()
    {
        return state == State.DISABLED;
    }
}
