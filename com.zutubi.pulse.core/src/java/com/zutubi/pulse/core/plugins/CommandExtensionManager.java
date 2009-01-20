package com.zutubi.pulse.core.plugins;

import com.zutubi.pulse.core.PulseFileLoaderFactory;
import com.zutubi.pulse.core.engine.CommandConfiguration;
import com.zutubi.pulse.core.tove.config.ConfigurationRegistry;
import com.zutubi.tove.type.TypeException;
import com.zutubi.util.logging.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

/**
 * Extension manager for managing build commands (e.g. the Ant command).
 */
public class CommandExtensionManager extends AbstractExtensionManager
{
    private static final Logger LOG = Logger.getLogger(CommandExtensionManager.class);

    private PulseFileLoaderFactory fileLoaderFactory;
    private ConfigurationRegistry configurationRegistry;

    protected String getExtensionPointId()
    {
        return "com.zutubi.pulse.core.commands";
    }

    protected void handleConfigurationElement(IExtension extension, IExtensionTracker tracker, IConfigurationElement config)
    {
        String name = config.getAttribute("name");
        String cls = config.getAttribute("class");

        Class<?> clazz = loadClass(extension, cls);
        if(clazz == null)
        {
            LOG.severe(String.format("Ignoring command '%s': class '%s' does not exist", name, cls));
            return;
        }

        if (!CommandConfiguration.class.isAssignableFrom(clazz))
        {
            LOG.severe(String.format("Ignoring command '%s': class '%s' does not implement CommandConfiguration", name, cls));
            return;
        }

        @SuppressWarnings("unchecked")
        Class<? extends CommandConfiguration> commandClass = (Class<? extends CommandConfiguration>) clazz;
        try
        {
            configurationRegistry.registerConfigurationType(commandClass);
        }
        catch (TypeException e)
        {
            LOG.severe("Registering command '" + name + "': " + e.getMessage(), e);
            return;
        }

        if (PluginManager.VERBOSE_EXTENSIONS)
        {
            System.out.println(String.format("Adding Command: %s -> %s", name, cls));
        }
        fileLoaderFactory.register(name, clazz);
        tracker.registerObject(extension, name, IExtensionTracker.REF_WEAK);
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

    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }
}
