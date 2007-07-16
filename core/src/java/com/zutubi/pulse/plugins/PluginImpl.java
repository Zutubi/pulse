package com.zutubi.pulse.plugins;

import org.osgi.framework.Bundle;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.framework.util.Headers;

import java.io.File;

/**
 * Metadata that describes a plugin and its current state.
 */
public class PluginImpl implements Plugin
{
    private static final String HEADER_NAME = "Bundle-Name";
    private static final String HEADER_DESCRIPTION = "Bundle-Description";
    private static final String HEADER_VENDOR = "Bundle-Vendor";
    private static final String HEADER_VERSION = "Bundle-Version";
    private static final String HEADER_SYMBOLICNAME = "Bundle-SymbolicName";

    enum Type
    {
        INTERNAL,
        PREPACKAGED,
        USER
    }

    private Headers manifest;
    private BundleDescription bundleDescription;
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

    public PluginImpl(Headers manifest, File pluginFile, State state, Type type)
    {
        this.manifest = manifest;
        this.pluginFile = pluginFile;
        this.state = state;
        this.type = type;
    }

    Headers getManifest()
    {
        return manifest;
    }

    BundleDescription getBundleDescription()
    {
        return bundleDescription;
    }

    public String getId()
    {
        return getSymbolicName();
    }

    public String getName()
    {
        String name = (String) manifest.get(HEADER_NAME);
        return name == null ? getId() : name;
    }

    public String getDescription()
    {
        return (String) manifest.get(HEADER_DESCRIPTION);
    }

    public String getVersion()
    {
        return (String) manifest.get(HEADER_VERSION);
    }

    public String getVendor()
    {
        return (String) manifest.get(HEADER_VENDOR);
    }

    public String getSymbolicName()
    {
        return (String) manifest.get(HEADER_SYMBOLICNAME);
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

    void setBundleDescription(BundleDescription bundleDescription)
    {
        this.bundleDescription = bundleDescription;
    }

    void setBundle(Bundle bundle)
    {
        this.bundle = bundle;
    }

    public boolean isEnabled()
    {
        return state == State.ENABLED;
    }

    public boolean canEnable()
    {
        return isDisabled() || isDisabling();
    }

    public boolean isDisabled()
    {
        return state == State.DISABLED;
    }

    public boolean canDisable()
    {
        return isEnabled();
    }

    public boolean isDisabling()
    {
        return state == State.DISABLING;
    }

    public boolean isUninstalling()
    {
        return state == State.UNINSTALLING;
    }

    public boolean canUninstall()
    {
        return isEnabled() || isDisabled();
    }

    public boolean isUpdating()
    {
        return state == State.UPDATING;
    }

    public boolean canUpdate()
    {
        return isEnabled() || isDisabled();
    }

    public boolean equals(Object obj)
    {
        if(obj == null || !(obj instanceof PluginImpl))
        {
            return false;
        }

        PluginImpl other = (PluginImpl) obj;
        return getId().equals(other.getId()) && getVersion().equals(other.getVersion());
    }

    public int hashCode()
    {
        return getId().hashCode() + 31 * getVersion().hashCode();
    }
}
