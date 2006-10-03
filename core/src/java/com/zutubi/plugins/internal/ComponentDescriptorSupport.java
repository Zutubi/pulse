package com.zutubi.plugins.internal;

import com.zutubi.plugins.ComponentDescriptor;
import com.zutubi.plugins.Plugin;
import nu.xom.Element;

/**
 * <class-comment/>
 */
public abstract class ComponentDescriptorSupport implements ComponentDescriptor
{
    private String name;
    private String key;
    private String description;

    private boolean enabled = false;

    protected Plugin plugin = null;

    public void init(Element config)
    {
        setName(config.getAttributeValue("name"));
        setKey(config.getAttributeValue("key"));

        Element desc = config.getFirstChildElement("description");
        if (desc != null)
        {
            setDescription(desc.getValue());
        }
    }

    public void destory()
    {

    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Override this method if you need to implement specific enable handling.
     */
    public void enable()
    {
        this.enabled = true;
    }

    /**
     * Override this method if you need to implement specific disable handling.
     */
    public void disable()
    {
        this.enabled = false;
    }

    public void setPlugin(Plugin plugin)
    {
        this.plugin = plugin;
    }
}
