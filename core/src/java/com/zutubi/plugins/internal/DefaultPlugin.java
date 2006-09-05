package com.zutubi.plugins.internal;

import com.zutubi.plugins.ComponentDescriptor;
import com.zutubi.plugins.Plugin;
import com.zutubi.plugins.PluginInformation;
import com.zutubi.plugins.utils.ClassLoaderUtils;
import com.zutubi.plugins.classloader.PluginsClassLoader;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class DefaultPlugin implements Plugin
{
    /**
     * The machine readable name of this plugin.
     */
    private String key;

    /**
     * The human readable name of this plugin.
     */
    private String name;

    /**
     * The plugin information for this plugin instance.
     */
    private PluginInformation info;

    /**
     * The component descriptors defined by this plugin.  It is these component  descriptors that define
     * the behaviour of this plugin.
     */
    private List<ComponentDescriptor> componentDescriptors = new LinkedList<ComponentDescriptor>();

    /**
     * Is this plugin currently enabled?
     */
    private boolean enabled;

    /**
     * A hidden plugin is one that by default should not be shown to the user.
     */
    private boolean hidden;

    /**
     * The URL from which this plugin was loaded.  If this is null, then the plugin was loaded from the
     * system classpath.  A plugin that is loaded from outside the system classpath can be moved and reloaded
     * at runtime.
     */
    private URL source;

    protected ClassLoader loader;

    public DefaultPlugin()
    {

    }

    public void setLoader(ClassLoader loader)
    {
        this.loader = loader;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    protected void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void enable()
    {
        if (!this.isEnabled())
        {
            for (ComponentDescriptor descriptor : componentDescriptors)
            {
                if (!descriptor.isEnabled())
                {
                    descriptor.enable();
                }
            }
            this.enabled = true;
        }
    }

    public void disable()
    {
        if (this.isEnabled())
        {
            for (ComponentDescriptor descriptor : componentDescriptors)
            {
                if (descriptor.isEnabled())
                {
                    descriptor.disable();
                }
            }
            this.enabled = false;
        }
    }

    public boolean isHidden()
    {
        return hidden;
    }

    protected void setHidden(boolean hidden)
    {
        this.hidden = hidden;
    }

    public PluginInformation getInfo()
    {
        return info;
    }

    protected void setInfo(PluginInformation info)
    {
        this.info = info;
    }

    public String getName()
    {
        return name;
    }

    protected void setName(String name)
    {
        this.name = name;
    }

    public String getKey()
    {
        return key;
    }

    protected void setKey(String key)
    {
        this.key = key;
    }

    public List<ComponentDescriptor> getComponentDescriptors()
    {
        return componentDescriptors;
    }

    protected void setComponentDescriptors(List<ComponentDescriptor> componentDescriptors)
    {
        this.componentDescriptors = componentDescriptors;
    }

    protected void addComponentDescriptor(ComponentDescriptor descriptor)
    {
        this.componentDescriptors.add(descriptor);
    }

    public URL getSource()
    {
        return source;
    }

    public void setSource(URL source)
    {
        this.source = source;
    }

    public Class loadClass(String className, Class callingClass) throws ClassNotFoundException
    {
        if (loader != null)
        {
            return loader.loadClass(className);
        }
        else
        {
            return ClassLoaderUtils.loadClass(className, callingClass);
        }
    }

    public InputStream getResourceAsStream(String resourceName)
    {
        if (loader != null)
        {
            return loader.getResourceAsStream(resourceName);
        }
        else
        {
            return ClassLoaderUtils.getResourceAsStream(resourceName, getClass());
        }
    }

    public void close()
    {
        if (loader != null && PluginsClassLoader.class.isAssignableFrom(loader.getClass()))
        {
            ((PluginsClassLoader) loader).close();
        }
    }
}
