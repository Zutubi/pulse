package com.zutubi.plugins.internal.loaders;

import com.zutubi.plugins.Plugin;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class ClassPathPluginLoader extends PluginLoaderSupport
{

    public ClassPathPluginLoader()
    {
    }

    public ClassPathPluginLoader(String descriptorResource)
    {
        setDescriptor(descriptorResource);
    }

    public List<Plugin> loadPlugins()
    {
        try
        {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null)
            {
                loader = getClass().getClassLoader();
            }

            Enumeration<URL> pluginDefinitions = loader.getResources(resource);
            return loadPlugins(pluginDefinitions, loader, null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return new LinkedList<Plugin>();
        }
    }
}
