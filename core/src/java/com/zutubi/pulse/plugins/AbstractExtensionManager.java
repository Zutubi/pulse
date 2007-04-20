package com.zutubi.pulse.plugins;

import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IFilter;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.registry.osgi.OSGIUtils;
import org.osgi.framework.Bundle;
import com.zutubi.util.logging.Logger;
import com.zutubi.pulse.core.plugins.CommandExtensionManager;
import com.zutubi.pulse.core.PulseFileLoaderFactory;

/**
 * A convenient base class for common extension manager implementations.
 */
public abstract class AbstractExtensionManager implements IExtensionChangeHandler, ExtensionManager
{
    private static final Logger LOG = Logger.getLogger(AbstractExtensionManager.class);

    protected PluginManager pluginManager;

    public void init()
    {
        pluginManager.registerExtensionManager(this);
    }

    protected abstract String getExtensionPointId();

    public void initialiseExtensions()
    {
        IExtensionRegistry registry = pluginManager.getExtenstionRegistry();
        IExtensionTracker tracker = pluginManager.getExtenstionTracker();

        IExtensionPoint extensionPoint = registry.getExtensionPoint(getExtensionPointId());
        if(extensionPoint == null)
        {
            LOG.severe("Extension point '" + getExtensionPointId() + "' not found.");
            return;
        }

        IFilter filter = ExtensionTracker.createExtensionPointFilter(extensionPoint);
        tracker.registerHandler(this, filter);
        IExtension[] extensions = extensionPoint.getExtensions();
        for (IExtension extension : extensions)
        {
            addExtension(tracker, extension);
        }
    }

    public void addExtension(IExtensionTracker tracker, IExtension extension)
    {
        IConfigurationElement[] configs = extension.getConfigurationElements();
        for (IConfigurationElement config : configs)
        {
            try
            {
                handleConfigurationElement(extension, tracker, config);
            }
            catch (InvalidRegistryObjectException e)
            {
                // what causes this?
                LOG.error(e);
            }
        }
    }

    protected abstract void handleConfigurationElement(IExtension extension, IExtensionTracker tracker, IConfigurationElement config);

    protected Class loadClass(IExtension extension, String cls)
    {
        Class clazz = null;
        try
        {
            Bundle bundle = OSGIUtils.getDefault().getBundle(extension.getNamespaceIdentifier());
            clazz = bundle.loadClass(cls);
        }
        catch (ClassNotFoundException e)
        {
            LOG.warning("Failed to add extension class: " + cls + ". Cause: " + e.getMessage(), e);
            handleExtensionError(extension, e);
        }
        catch (NoClassDefFoundError e)
        {
            LOG.warning("Failed to add extension class: " + cls + ". Cause: " + e.getMessage(), e);
            handleExtensionError(extension, e);
        }

        return clazz;
    }

    protected void handleExtensionError(IExtension extension, Throwable t)
    {
        try
        {
            // now lets record it for the UI.
            Plugin plugin = pluginManager.getPlugin(extension);
            pluginManager.disablePlugin(plugin);
            // add the error message to the plugin...
            t.getMessage();
        }
        catch (Throwable e)
        {
            LOG.error(e);
        }
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }
}
