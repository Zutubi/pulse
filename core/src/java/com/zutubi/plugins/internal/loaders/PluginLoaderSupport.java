package com.zutubi.plugins.internal.loaders;

import com.zutubi.plugins.ComponentDescriptorFactory;
import com.zutubi.plugins.PluginLoader;
import com.zutubi.plugins.Plugin;
import com.zutubi.plugins.internal.DefaultPlugin;
import com.zutubi.plugins.internal.XMLPluginDescriptorSupport;
import com.zutubi.plugins.utils.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public abstract class PluginLoaderSupport implements PluginLoader
{
    private XMLPluginDescriptorSupport xmlDescriptorSupport = new XMLPluginDescriptorSupport();

    protected String resource = PLUGIN_DESCRIPTOR_NAME;

    public void setComponentDescriptorFactory(ComponentDescriptorFactory descriptorFactory)
    {
        xmlDescriptorSupport.setDescriptorFactory(descriptorFactory);
    }

    protected Plugin loadPlugin(URL descriptor, ClassLoader loader, URL source)
    {
        InputStream input = null;
        try
        {
            input = descriptor.openStream();

            DefaultPlugin plugin = new DefaultPlugin();
            plugin.setLoader(loader);
            plugin.setSource(source);

            return xmlDescriptorSupport.loadPlugin(input, plugin);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            IOUtils.close(input);
        }
    }

    protected List<Plugin> loadPlugins(Enumeration<URL> pluginDefs, ClassLoader classLoader, URL source)
    {
        LinkedList<Plugin> plugins = new LinkedList<Plugin>();
        for(URL pluginDefinition : toCollection(pluginDefs))
        {
            Plugin plugin = loadPlugin(pluginDefinition, classLoader, source);
            if (plugin != null)
            {
                plugins.add(plugin);
            }
        }
        return plugins;
    }

    private <T> Collection<T> toCollection(Enumeration<T> enumeration)
    {
        List<T> c = new LinkedList<T>();
        while (enumeration.hasMoreElements())
        {
            c.add(enumeration.nextElement());
        }
        return c;
    }

    public void setDescriptor(String resource)
    {
        this.resource = resource;
    }
}
