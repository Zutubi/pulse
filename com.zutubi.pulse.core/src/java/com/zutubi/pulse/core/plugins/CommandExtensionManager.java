package com.zutubi.pulse.core.plugins;

import com.zutubi.pulse.core.PulseFileLoaderFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

/**
 * Extension manager for managing build commands (e.g. the Ant command).
 */
public class CommandExtensionManager extends AbstractExtensionManager
{
    private PulseFileLoaderFactory fileLoaderFactory;

    protected String getExtensionPointId()
    {
        return "com.zutubi.pulse.core.commands";
    }

    protected void handleConfigurationElement(IExtension extension, IExtensionTracker tracker, IConfigurationElement config)
    {
        String name = config.getAttribute("name");
        String cls = config.getAttribute("class");
        if (PluginManager.VERBOSE_EXTENSIONS)
        {
            System.out.println(String.format("Adding Command: %s -> %s", name, cls));
        }

        Class clazz = loadClass(extension, cls);
        if(clazz != null)
        {
            fileLoaderFactory.register(name, clazz);
            tracker.registerObject(extension, name, IExtensionTracker.REF_WEAK);
        }
    }

    public void removeExtension(IExtension extension, Object[] objects)
    {
        for (Object o : objects)
        {
            fileLoaderFactory.unregister((String) o);
        }
    }

    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }
}
