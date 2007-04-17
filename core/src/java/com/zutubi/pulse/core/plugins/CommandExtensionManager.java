package com.zutubi.pulse.core.plugins;

import com.zutubi.pulse.core.PulseFileLoaderFactory;
import com.zutubi.pulse.plugins.Plugin;
import com.zutubi.pulse.plugins.PluginManager;
import com.zutubi.pulse.plugins.ExtensionManager;
import com.zutubi.pulse.util.logging.Logger;
import org.eclipse.core.internal.registry.osgi.OSGIUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IFilter;
import org.osgi.framework.Bundle;

/**
 */
public class CommandExtensionManager implements IExtensionChangeHandler, ExtensionManager
{
    private static final Logger LOG = Logger.getLogger(CommandExtensionManager.class);

    private PluginManager pluginManager;
    private PulseFileLoaderFactory fileLoaderFactory;

    public void init()
    {
        pluginManager.registerExtensionManager(this);
    }

    public void initialiseExtensions()
    {
        IExtensionRegistry registry = pluginManager.getExtenstionRegistry();
        IExtensionTracker tracker = pluginManager.getExtenstionTracker();

        IExtensionPoint extensionPoint = registry.getExtensionPoint("com.zutubi.pulse.core.commands");
        if(extensionPoint == null)
        {
            LOG.severe("Commands extension point not found.");
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
                String name = config.getAttribute("name");
                String cls = config.getAttribute("class");
                try
                {
                    System.out.println(String.format("addExtension: %s -> %s", name, cls));

                    Bundle bundle = OSGIUtils.getDefault().getBundle(extension.getNamespaceIdentifier());
                    Class clazz = bundle.loadClass(cls);
                    fileLoaderFactory.register(name, clazz);
                    tracker.registerObject(extension, name, IExtensionTracker.REF_WEAK);
                }
                catch (ClassNotFoundException e)
                {
                    LOG.warning("Failed to add extension, name: " + name + ", class: " + cls + ". Cause: " + e.getMessage(), e);
                    handleExtensionError(extension, e);
                }
                catch (NoClassDefFoundError e)
                {
                    LOG.warning("Failed to add extension, name: " + name + ", class: " + cls + ". Cause: " + e.getMessage(), e);
                    handleExtensionError(extension, e);
                }
            }
            catch (InvalidRegistryObjectException e)
            {
                // what causes this?
                LOG.error(e);
            }
        }
    }

    private void handleExtensionError(IExtension extension, Throwable t)
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

    public void removeExtension(IExtension extension, Object[] objects)
    {
        for (Object o : objects)
        {
            fileLoaderFactory.unregister((String) o);
        }
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }

    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }
}
