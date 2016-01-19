package com.zutubi.pulse.core.plugins;

import com.google.common.base.Objects;
import com.zutubi.util.StringUtils;
import org.eclipse.osgi.framework.util.Headers;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The local plugin is a wrapper around a locally deployed OSGi bundle.  The
 * plugin details are read from the OSGi Manifest headers.
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

    private Bundle bundle;

    private List<String> errorMessages = new LinkedList<String>();

    private State pluginState;

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
        Object entry = manifest.get(HEADER_SYMBOLICNAME);
        if (entry == null)
        {
            return null;
        }
        String[] nextToken = StringUtils.getNextToken((String) entry, ';', true);
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

    public PluginVersion getVersion()
    {
        return new PluginVersion((String) manifest.get(HEADER_VERSION));
    }

    public URI getSource()
    {
        return source.toURI();
    }

    public State getState()
    {
        return this.pluginState;
    }

    public boolean isRunning()
    {
        switch (pluginState)
        {
            case ENABLED:
            case DISABLING:
            case INSTALLING:
            case UPGRADING:
                return true;
            default:
                return false;
        }
    }

    void setState(State pluginState)
    {
        this.pluginState = pluginState;
    }

    public List<String> getErrorMessages()
    {
        return Collections.unmodifiableList(errorMessages);
    }

    public void addErrorMessage(String errorMessage)
    {
        errorMessages.add(errorMessage);
    }

    public void clearErrorMessages()
    {
        errorMessages.clear();
    }
    
    public void enable() throws PluginException
    {
        clearErrorMessages();
        switch (pluginState)
        {
            case UNINSTALLING:
                throw new PluginException("Unable to enable plugin: already marked for uninstall");
            case UPGRADING:
                throw new PluginException("Unable to enable plugin: already marked for update");
            case DISABLING:
            case INSTALLING:
                manager.enablePlugin(this);
            case DISABLED:
            case ERROR:
                manager.enablePlugin(this);
                break;
            case ENABLED:
                break;
        }
    }

    public void disable() throws PluginException
    {
        clearErrorMessages();
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
            case UPGRADING:
                throw new PluginException("Unable to disable plugin: already marked for upgrade");
            default:
                manager.disablePlugin(this);
        }
    }

    public void uninstall() throws PluginException
    {
        clearErrorMessages();
        switch (pluginState)
        {
            case ENABLED:
                manager.requestUninstall(this);
                break;
            case UNINSTALLING:
                throw new PluginException("Cannot uninstall plugin: already marked for uninstall");
            case UPGRADING:
                throw new PluginException("Cannot uninstall plugin: already marked for upgrade");
            default:
                manager.uninstallPlugin(this);
                break;
        }
    }

    public Plugin upgrade(URI newSource) throws PluginException
    {
        clearErrorMessages();
        switch (pluginState)
        {
            case ENABLED:
                manager.requestUpgrade(this, newSource);
                return this;
            case UPGRADING:
                manager.cancelUpgrade(this);
                manager.requestUpgrade(this, newSource);
                return this;
            case UNINSTALLING:
                throw new PluginException("Unable to update plugin: already marked for uninstall");
            default:
                return manager.upgradePlugin(this, newSource);
        }
    }

    public List<PluginDependency> getRequiredPlugins()
    {
        // note that this only really gives a correct answer if this plugin was resolved.
        // Ie: if it fails to resolve a dependency, then this call will return 0, even though
        // we know there is at least one dependency.
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
        return isDisabled() || isDisabling() || isError();
    }

    public boolean isDisabled()
    {
        return pluginState == Plugin.State.DISABLED;
    }

    public boolean canDisable()
    {
        return isEnabled() || isError();
    }

    public boolean isDisabling()
    {
        return pluginState == Plugin.State.DISABLING;
    }

    public boolean isError()
    {
        return pluginState == Plugin.State.ERROR;
    }
    
    public boolean isInstalling()
    {
        return pluginState == Plugin.State.INSTALLING;
    }

    public boolean isUninstalling()
    {
        return pluginState == Plugin.State.UNINSTALLING;
    }

    public boolean canUninstall()
    {
        return !isUninstalling() && !isUpgrading();
    }

    public boolean isUpgrading()
    {
        return pluginState == Plugin.State.UPGRADING;
    }

    public boolean canUpdate()
    {
        return isEnabled() || isDisabled() || isError();
    }

    public boolean equals(Object obj)
    {
        if(obj == null || !(obj instanceof LocalPlugin))
        {
            return false;
        }

        LocalPlugin other = (LocalPlugin) obj;
        return Objects.equal(getId(), other.getId()) && getVersion().equals(other.getVersion());
    }

    public int hashCode()
    {
        return getId().hashCode() + 31 * getVersion().hashCode();
    }

    public Class<?> loadClass(String type) throws ClassNotFoundException
    {
        if (getBundle() == null)
        {
            throw new IllegalStateException("Can not load a class from plugin: " + getName() + ". No bundle " +
                    "has been loaded.  Plugin state: " + getState());
        }
        return getBundle().loadClass(type);
    }

    protected abstract Headers loadPluginManifest(File pluginFile);

    protected Headers parseManifest(InputStream manifestIn) throws BundleException
    {
        Headers headers = Headers.parseManifest(manifestIn);
        checkRequiredHeader(headers, HEADER_NAME);
        checkRequiredHeader(headers, HEADER_SYMBOLICNAME);
        checkRequiredHeader(headers, HEADER_VERSION);

        // Validates the version
        new PluginVersion((String) headers.get(HEADER_VERSION));

        return headers;
    }

    private void checkRequiredHeader(Headers headers, String header)
    {
        if (!headers.containsKey(header))
        {
            throw new IllegalArgumentException("Required header '" + header + "' not present in manifest");
        }
    }

    public Bundle getBundle()
    {
        return bundle;
    }

    /**
     * Links this plugin with a successfully installed Equinox bundle.
     * 
     * @param bundle            the bundle to link with
     */
    public void associateBundle(Bundle bundle)
    {
        this.bundle = bundle;
    }

    /**
     * Removes all links to an Equinox bundle due to an error and moves this
     * plugin to the error state.
     * 
     * @param errorMessage a message describing what went wrong
     */
    public void disassociateBundle(String errorMessage)
    {
        bundle = null;
        pluginState = State.ERROR;
        addErrorMessage(errorMessage);
    }

    @Override
    public String toString()
    {
        return getId() + ":" + getVersion() + ":" + getState() + ":" + getErrorMessages();
    }
}
