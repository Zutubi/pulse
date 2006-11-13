package com.zutubi.plugins.internal.loaders;

import com.zutubi.plugins.Plugin;
import com.zutubi.plugins.classloader.DirectoryClassLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class DirectoryPluginLoader extends PluginLoaderSupport
{
    private File base = null;

    public DirectoryPluginLoader()
    {
    }

    public DirectoryPluginLoader(File base)
    {
        setBaseDirectory(base);
    }

    public void setBaseDirectory(File base)
    {
        if (base == null)
        {
            throw new IllegalArgumentException();
        }
        if (!base.isDirectory())
        {
            throw new IllegalArgumentException();
        }
        this.base = base;
    }

    public List<Plugin> loadPlugins()
    {
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        if (parent == null)
        {
            parent = getClass().getClassLoader();
        }

        try
        {
            DirectoryClassLoader loader = new DirectoryClassLoader(base, parent);
            
            URL pluginDescriptor = loader.findResource(resource);
            Plugin plugin = loadPlugin(pluginDescriptor, loader, base.toURI().toURL());

            List<Plugin> plugins = new LinkedList<Plugin>();
            if (plugin != null)
            {
                plugins.add(plugin);
            }
            return plugins;
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            return new LinkedList<Plugin>();
        }
    }
}
