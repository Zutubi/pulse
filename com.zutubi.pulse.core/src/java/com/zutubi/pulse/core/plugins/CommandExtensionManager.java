package com.zutubi.pulse.core.plugins;

import com.zutubi.pulse.core.commands.api.CommandConfiguration;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.tove.config.ConfigurationRegistry;
import com.zutubi.tove.type.TypeException;
import com.zutubi.util.logging.Logger;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Extension manager for managing build commands (e.g. the Ant command).
 */
public class CommandExtensionManager extends AbstractExtensionManager
{
    private static final Logger LOG = Logger.getLogger(CommandExtensionManager.class);

    private static final String ELEMENT_RESOURCE = "resource";
    private static final String ATTRIBUTE_CLASS = "class";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_OPTIONAL = "optional";

    private Map<Class<? extends CommandConfiguration>, CommandInfo> typeToInfoMap = new HashMap<Class<? extends CommandConfiguration>, CommandInfo>();

    private PulseFileLoaderFactory fileLoaderFactory;
    private ConfigurationRegistry configurationRegistry;

    protected String getExtensionPointId()
    {
        return "com.zutubi.pulse.core.commands";
    }

    protected void handleConfigurationElement(IExtension extension, IExtensionTracker tracker, IConfigurationElement config)
    {
        String name = config.getAttribute(ATTRIBUTE_NAME);
        String cls = config.getAttribute(ATTRIBUTE_CLASS);

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

        if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
        {
            if (PluginManager.VERBOSE_EXTENSIONS)
            {
                System.out.println(String.format("Adding Command: %s -> %s", name, cls));
            }

            fileLoaderFactory.register(name, clazz);

            typeToInfoMap.put(commandClass, createCommandInfo(commandClass, config));
        }
        tracker.registerObject(extension, name, IExtensionTracker.REF_WEAK);
    }

    private CommandInfo createCommandInfo(Class<? extends CommandConfiguration> commandClass, IConfigurationElement config)
    {
        CommandInfo info = new CommandInfo(commandClass);
        for (IConfigurationElement resourceElement: config.getChildren(ELEMENT_RESOURCE))
        {
            info.addDefaultResourceRequirement(new ResourceRequirement(resourceElement.getAttribute(ATTRIBUTE_NAME), null, false, Boolean.valueOf(resourceElement.getAttribute(ATTRIBUTE_OPTIONAL))));
        }

        return info;
    }

    public Set<Class<? extends CommandConfiguration>> getCommandClasses()
    {
        return typeToInfoMap.keySet();
    }

    public List<ResourceRequirement> getDefaultResourceRequirements(Class<? extends CommandConfiguration> commandType)
    {
        CommandInfo info = typeToInfoMap.get(commandType);
        if (info == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return Collections.unmodifiableList(info.getDefaultResourceRequirements());
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

    public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
    {
        this.configurationRegistry = configurationRegistry;
    }

    /**
     * Holds information about a registered command.
     */
    private static class CommandInfo
    {
        private Class<? extends CommandConfiguration> configClass;
        private List<ResourceRequirement> defaultResourceRequirements = new LinkedList<ResourceRequirement>();

        private CommandInfo(Class<? extends CommandConfiguration> configClass)
        {
            this.configClass = configClass;
        }

        public Class<? extends CommandConfiguration> getConfigClass()
        {
            return configClass;
        }

        public List<ResourceRequirement> getDefaultResourceRequirements()
        {
            return defaultResourceRequirements;
        }

        public void addDefaultResourceRequirement(ResourceRequirement requirement)
        {
            defaultResourceRequirements.add(requirement);
        }
    }
}
