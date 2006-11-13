package com.zutubi.pulse.plugins;

import com.zutubi.plugins.ComponentDescriptor;
import com.zutubi.plugins.Plugin;
import com.zutubi.plugins.internal.DefaultComponentDescriptorFactory;
import com.zutubi.plugins.internal.loaders.ClassPathPluginLoader;
import com.zutubi.plugins.repositories.LocalPluginRepository;
import com.zutubi.pulse.core.ObjectFactory;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <class-comment/>
 */
public class PulsePluginManager
{
    private LocalPluginRepository repository;

    private ClassPathPluginLoader classPathLoader;

    private Map<String, Class<? extends ComponentDescriptor>> descriptors = new HashMap<String, Class<? extends ComponentDescriptor>>();

    private List<String> systemPluginDescriptors = new LinkedList<String>();

    private File pluginDirectory = null;

    private List<Plugin> plugins;

    private ObjectFactory objectFactory;

    /**
     *
     * @param dir
     */
    public void setPluginDirectory(File dir)
    {
        this.pluginDirectory = dir;
    }

    public void setSystemPluginDescriptors(List<String> systemPluginDescriptors)
    {
        this.systemPluginDescriptors = systemPluginDescriptors;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    /**
     *
     * @param descriptors
     */
    public void setDescriptors(Map<String, Class<? extends ComponentDescriptor>> descriptors)
    {
        this.descriptors = descriptors;
    }

    public void init()
    {
        DefaultComponentDescriptorFactory descriptorFactory = new DefaultComponentDescriptorFactory();
        descriptorFactory.setObjectFactory(new PluginsObjectFactoryAdaptor(objectFactory));
        descriptorFactory.setDescriptors(descriptors);

        plugins = new LinkedList<Plugin>();

        // initialise the plugin repository
        if (pluginDirectory != null)
        {
            repository = new LocalPluginRepository(pluginDirectory);
            repository.setDescriptorFactory(descriptorFactory);
            repository.scanForPlugins();

            // load all of the plugins from the local plugin repository.
            plugins.addAll(repository.getPlugins());
        }

        // Initialise the class path loader.
        classPathLoader = new ClassPathPluginLoader();
        classPathLoader.setComponentDescriptorFactory(descriptorFactory);

        // load all of the plugins from the system classpath.
        for (String descriptor : systemPluginDescriptors)
        {
            classPathLoader.setDescriptor(descriptor);
            plugins.addAll(classPathLoader.loadPlugins());
        }

        // assume for now that all of the plugins need to be enbled.
        for (Plugin plugin : plugins)
        {
            plugin.enable();
        }
    }

    public void install(Plugin plugin)
    {
        if (repository.containsPlugin(plugin))
        {
            throw new IllegalArgumentException();
        }
        repository.installPlugin(plugin);
    }


    public List<Plugin> getPlugins()
    {
        return plugins;
    }

    public void destory()
    {
        if (repository != null)
        {
            repository.destory();
        }

        for (Plugin plugin : plugins)
        {
            plugin.close();
        }
    }

    public void uninstall(Plugin plugin)
    {
        //TODO: how does a client know whether or not a plugin belongs to the repository, and can therefore be uninstalled?
        if (!repository.containsPlugin(plugin))
        {
            throw new IllegalArgumentException();
        }
        repository.uninstallPlugin(plugin);
    }

}
