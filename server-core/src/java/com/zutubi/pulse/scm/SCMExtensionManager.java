package com.zutubi.pulse.scm;

import com.zutubi.pulse.plugins.AbstractExtensionManager;
import com.zutubi.pulse.plugins.PluginManager;
import com.zutubi.pulse.core.PulseFileLoaderFactory;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

/**
 * Extension manager for managing build commands (e.g. the Ant command).
 */
public class SCMExtensionManager extends AbstractExtensionManager
{
    protected String getExtensionPointId()
    {
        return "com.zutubi.pulse.servercore.scms";
    }

    protected void handleConfigurationElement(IExtension extension, IExtensionTracker tracker, IConfigurationElement config)
    {
        String name = config.getAttribute("name");
        String configClass = config.getAttribute("config-class");
        String clientClass = config.getAttribute("client-class");
        System.out.println(String.format("Adding SCM: %s -> (%s, %s)", name, configClass, clientClass));

        Class configClazz = loadClass(extension, configClass);
        Class clientClazz = loadClass(extension, clientClass);
        if(configClazz != null && clientClazz != null)
        {
            tracker.registerObject(extension, name, IExtensionTracker.REF_WEAK);
        }
    }

    public void removeExtension(IExtension extension, Object[] objects)
    {
        for (Object o : objects)
        {
        }
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }
}
