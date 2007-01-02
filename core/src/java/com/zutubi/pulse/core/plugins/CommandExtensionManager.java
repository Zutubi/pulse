package com.zutubi.pulse.core.plugins;

import com.zutubi.pulse.core.PulseFileLoaderFactory;
import com.zutubi.pulse.plugins.PluginManager;
import org.eclipse.core.internal.registry.osgi.OSGIUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IFilter;
import org.osgi.framework.Bundle;

/**
 */
public class CommandExtensionManager implements IExtensionChangeHandler
{
    private PluginManager pluginManager;
    private PulseFileLoaderFactory fileLoaderFactory;

    public void init()
    {
        IExtensionRegistry registry = pluginManager.getExtenstionRegistry();
        IExtensionTracker tracker = pluginManager.getExtenstionTracker();

        IExtensionPoint extensionPoint = registry.getExtensionPoint("com.zutubi.pulse.core.commands");
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
                Bundle bundle = OSGIUtils.getDefault().getBundle(extension.getNamespaceIdentifier());
                Class clazz = bundle.loadClass(config.getAttribute("class"));
                fileLoaderFactory.register(name, clazz);
                tracker.registerObject(extension, name, IExtensionTracker.REF_WEAK);
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void removeExtension(IExtension extension, Object[] objects)
    {
        for(Object o: objects)
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
