package com.zutubi.pulse.core.plugins;

import com.zutubi.util.logging.Logger;
import org.eclipse.core.internal.registry.osgi.OSGIUtils;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IFilter;
import org.osgi.framework.Bundle;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A convenient base class for common extension manager implementations.
 */
public abstract class AbstractExtensionManager implements IExtensionChangeHandler, ExtensionManager
{
    private static final Logger LOG = Logger.getLogger(AbstractExtensionManager.class);

    public static final String CONFIG_EXTENSION_POINT = "com.zutubi.pulse.core.config";

    protected PluginManager pluginManager;

    public void init()
    {
        pluginManager.registerExtensionManager(this);
    }

    protected abstract String getExtensionPointId();

    public void initialiseExtensions()
    {
        IExtensionRegistry registry = pluginManager.getExtensionRegistry();
        IExtensionTracker tracker = pluginManager.getExtensionTracker();

        IExtensionPoint extensionPoint = registry.getExtensionPoint(getExtensionPointId());
        if (extensionPoint == null)
        {
            // check that the MANIFEST.MF files are being copied into the classpath. ie:
            // ensure that IDEA treating *.mf files are resources that need to be copied into the
            // classes directory.
            LOG.severe("Extension point '" + getExtensionPointId() + "' not found.");
            return;
        }

        IFilter filter = ExtensionTracker.createExtensionPointFilter(extensionPoint);
        tracker.registerHandler(this, filter);
        IExtension[] extensions = extensionPoint.getExtensions();
        for (IExtension extension : pluginManager.sortExtensions(extensions))
        {
            try
            {
                addExtension(tracker, extension);
            }
            catch (Throwable e)
            {
                LOG.severe("Error while adding extension at point '" + getExtensionPointId() + "' for plugin '" + extension.getContributor().getName() + "': " + e.getMessage(), e);
            }
        }
    }

    public void addExtension(IExtensionTracker tracker, IExtension extension)
    {
        synchronized (pluginManager)
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
            LOG.warning("Failed to add extension class: " + cls + " due to ClassNotFoundException. Cause: " + e.getMessage());
            LOG.info(e);
        }
        catch (NoClassDefFoundError e)
        {
            LOG.warning("Failed to add extension class: " + cls + " due to NoClassDefFoundError. Cause: " + e.getMessage());
            LOG.info(e);
        }
        catch (Throwable t)
        {
            LOG.warning("Failed to load extension class: " + cls + " due to " + t.getClass().getName() + ". Cause: " + t.getMessage());
            LOG.info(t);
        }

        return clazz;
    }

    protected void handleExtensionError(IExtension extension, Throwable t)
    {
        handleExtensionError(extension, t.getClass().getName() + ": " + t.getMessage());
    }

    protected void handleExtensionError(IExtension extension, String message)
    {
        try
        {
            Plugin plugin = getPlugin(extension);
            if (plugin != null)
            {
                plugin.addErrorMessage(message);
            }
        }
        catch (Throwable e)
        {
            LOG.error(e);
        }
    }

    /**
     * A helper that returns all &lt;config&gt; elements for configuration
     * extensions that are contributed by the same bundle as the given
     * extension.  This allows extension managers to correlate configuration
     * classes with other extensions (e.g. the ScmConfiguration related to an
     * Scm implementation).
     *
     * @param extension used to determine the contributing bundle
     * @return all &lt;config&gt; elements for configuration extensions defined
     *         within the same bundle as the given extension
     */
    protected List<IConfigurationElement> getConfigElements(IExtension extension)
    {
        List<IConfigurationElement> configElements = new LinkedList<IConfigurationElement>();
        IExtensionRegistry registry = pluginManager.getExtensionRegistry();
        IExtension[] bundleExtensions = registry.getExtensions(extension.getNamespaceIdentifier());
        for (IExtension candidate : bundleExtensions)
        {
            if (candidate.getExtensionPointUniqueIdentifier().equals(CONFIG_EXTENSION_POINT))
            {
                configElements.addAll(Arrays.asList(candidate.getConfigurationElements()));
            }
        }

        return configElements;
    }

    private Plugin getPlugin(IExtension extension)
    {
        if (!extension.isValid())
        {
            return null;
        }
        Bundle bundle = OSGIUtils.getDefault().getBundle(extension.getNamespaceIdentifier());
        long requiredBundleId = bundle.getBundleId();

        for (Plugin plugin : pluginManager.getPlugins())
        {
            LocalPlugin localPlugin = (LocalPlugin) plugin;
            if (localPlugin.getBundle() != null)
            {
                if (localPlugin.getBundle().getBundleId() == requiredBundleId)
                {
                    return plugin;
                }
            }
        }
        return null;
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }
}
