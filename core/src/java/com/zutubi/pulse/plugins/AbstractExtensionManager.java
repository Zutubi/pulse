package com.zutubi.pulse.plugins;

import com.zutubi.util.logging.Logger;
import org.eclipse.core.internal.registry.osgi.OSGIUtils;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IFilter;
import org.osgi.framework.Bundle;

import java.util.LinkedList;
import java.util.List;

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
        handleExtensionError(extension, t.getMessage());
    }

    protected void handleExtensionError(IExtension extension, String message)
    {
        try
        {
            // Record the error for the UI.
            Plugin plugin = pluginManager.getPlugin(extension);
            pluginManager.disablePlugin(plugin, message);
        }
        catch (Throwable e)
        {
            LOG.error(e);
        }
    }

    /**
     * A helper that returns all <config> elements for configuration
     * extensions that are contributed by the same bundle as the given
     * extension.  This allows extension managers to correlate configuration
     * classes with other extensions (e.g. the ScmConfiguration related to an
     * Scm implementation).
     *
     * @param extension used to determine the contributing bundle
     * @return all <config> elements for configuration extensions defined
     *         within the same bundle as the given extension 
     */
    protected List<IConfigurationElement> getConfigElements(IExtension extension)
    {
        List<IConfigurationElement> configElements = new LinkedList<IConfigurationElement>();
        IExtensionRegistry registry = pluginManager.getExtenstionRegistry();
        IExtension[] bundleExtensions = registry.getExtensions(extension.getNamespaceIdentifier());
        for (IExtension candidate : bundleExtensions)
        {
            if (candidate.getExtensionPointUniqueIdentifier().equals(PluginManager.CONFIG_EXTENSION_POINT))
            {
                for (IConfigurationElement configElement : candidate.getConfigurationElements())
                {
                    configElements.add(configElement);
                }
            }
        }

        return configElements;
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }
}
