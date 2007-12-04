package com.zutubi.pulse.plugins;

import org.eclipse.osgi.framework.util.Headers;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.osgi.framework.Bundle;

import java.io.File;
import java.net.URI;
import java.util.List;

import com.zutubi.pulse.plugins.PluginException;
import com.zutubi.pulse.plugins.Version;
import com.zutubi.util.StringUtils;

/**
 *
 *
 */
public abstract class LocalPlugin implements Plugin
{
    private static final String HEADER_NAME = "Bundle-Name";
    private static final String HEADER_DESCRIPTION = "Bundle-Description";
    private static final String HEADER_VENDOR = "Bundle-Vendor";
    private static final String HEADER_VERSION = "Bundle-Version";
    private static final String HEADER_SYMBOLICNAME = "Bundle-SymbolicName";

    protected Headers manifest = null;

    protected File source;

    protected PluginManager manager;

    public Bundle bundle;
    protected BundleDescription bundleDescription;

    private String errorMessage;

    private State pluginState;
    private Type pluginType;

    public LocalPlugin(File source)
    {
        this.source = source;
        this.manifest = loadPluginManifest(source);
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

    public String getId()
    {
        String[] nextToken = StringUtils.getNextToken((String) manifest.get(HEADER_SYMBOLICNAME), ';', true);
        if(nextToken == null)
        {
            return null;
        }
        else
        {
            return nextToken[0];
        }
    }

    public String getVendor()
    {
        return (String) manifest.get(HEADER_VENDOR);
    }

    public Version getVersion()
    {
        return new Version((String) manifest.get(HEADER_VERSION));
    }

    public URI getSource()
    {
        return source.toURI();
    }

    public State getState()
    {
        return this.pluginState;
    }

    void setState(State pluginState)
    {
        this.pluginState = pluginState;
    }

    public Type getType()
    {
        return pluginType;
    }

    void setType(Type type)
    {
        this.pluginType = type;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public void enable() throws PluginException
    {
        setErrorMessage(null);
        switch (pluginState)
        {
            case UNINSTALLING:
                throw new PluginException("Unable to enable plugin: already marked for uninstall");
            case UPDATING:
                throw new PluginException("Unable to enable plugin: already marked for update");
            case DISABLING:
                manager.enablePlugin(this);
            case DISABLED:
                manager.enablePlugin(this);
                break;
            case ENABLED:
                break;
        }
    }

    public void disable() throws PluginException
    {
        disable(null);
    }

    public void disable(String reason) throws PluginException
    {
        setErrorMessage(reason);
        
        switch (pluginState)
        {
            case DISABLED:
            case DISABLING:
                break;
            case ENABLED:
                manager.requestDisable(this);
                break;
            case UNINSTALLING:
                throw new PluginException("Unable to disable plugin: already marked for uninstall");
            case UPDATING:
                throw new PluginException("Unable to disable plugin: already marked for update");
            default:
                manager.disablePlugin(this);
        }
    }

    public void uninstall() throws PluginException
    {
        if (pluginType == Type.INTERNAL)
        {
            throw new PluginException("Cannot uninstall plugin: this is an internal plugin.");
        }
        setErrorMessage(null);
        switch (pluginState)
        {
            case ENABLED:
                manager.requestUninstall(this);
                break;
            case UNINSTALLING:
                throw new PluginException("Cannot uninstall plugin: already marked for uninstall");
            case UPDATING:
                throw new PluginException("Cannot uninstall plugin: already marked for update");
            default:
                manager.uninstallPlugin(this);
                break;
        }
    }

    public Plugin upgrade(URI newSource) throws PluginException
    {
        setErrorMessage(null);
        switch (pluginState)
        {
            case ENABLED:
                manager.requestUpgrade(this, newSource);
                return this;
            case UPDATING:
                manager.cancelUpgrade(this);
                manager.requestUpgrade(this, newSource);
                return this;
            case UNINSTALLING:
                throw new PluginException("Unable to update plugin: already marked for uninstall");
            default:
                return manager.upgradePlugin(this, newSource);
        }
    }

    public void resolve() throws PluginException
    {
        setErrorMessage(null);
        switch (pluginState)
        {
            case VERSION_CHANGE:
                manager.resolveVersionChange(this);
                break;
            default:
                throw new PluginException("Unable to resolve plugin version: no version change detected.");
        }
    }

    public List<PluginRequirement> getRequiredPlugins()
    {
        return manager.getRequiredPlugins(this);
    }

    public List<Plugin> getDependentPlugins()
    {
        return manager.getDependentPlugins(this);
    }

    public boolean isEnabled()
    {
        return pluginState == Plugin.State.ENABLED;
    }

    public boolean canEnable()
    {
        return isDisabled() || isDisabling();
    }

    public boolean isDisabled()
    {
        return pluginState == Plugin.State.DISABLED;
    }

    public boolean canDisable()
    {
        return isEnabled();
    }

    public boolean isDisabling()
    {
        return pluginState == Plugin.State.DISABLING;
    }

    public boolean isUninstalling()
    {
        return pluginState == Plugin.State.UNINSTALLING;
    }

    public boolean canUninstall()
    {
        return (isEnabled() || isDisabled()) && pluginType != Type.INTERNAL;
    }

    public boolean isUpdating()
    {
        return pluginState == Plugin.State.UPDATING;
    }

    public boolean canUpdate()
    {
        return isEnabled() || isDisabled();
    }

    public boolean equals(Object obj)
    {
        if(obj == null || !(obj instanceof LocalPlugin))
        {
            return false;
        }

        LocalPlugin other = (LocalPlugin) obj;
        return getId().equals(other.getId()) && getVersion().equals(other.getVersion());
    }

    public int hashCode()
    {
        return getId().hashCode() + 31 * getVersion().hashCode();
    }

    public Class<?> loadClass(String type) throws ClassNotFoundException
    {
        return bundle.loadClass(type);
    }

    protected abstract Headers loadPluginManifest(File pluginFile);

}
