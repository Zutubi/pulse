package com.zutubi.plugins.internal;

import com.zutubi.plugins.ComponentDescriptor;
import com.zutubi.plugins.ComponentDescriptorFactory;
import com.zutubi.plugins.ObjectFactory;
import com.zutubi.plugins.Plugin;
import nu.xom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public class DefaultComponentDescriptorFactory implements ComponentDescriptorFactory
{
    private Map<String, Class<? extends ComponentDescriptor>> descriptors = new HashMap<String, Class<? extends ComponentDescriptor>>();

    private ObjectFactory objectFactory = new DefaultObjectFactory();

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void addDescriptor(String type, Class<? extends ComponentDescriptor> descriptor)
    {
        descriptors.put(type, descriptor);
    }

    public void setDescriptors(Map<String, Class<? extends ComponentDescriptor>> descriptors)
    {
        this.descriptors = descriptors;
    }

    public boolean supportsComponentDescriptor(String type)
    {
        return descriptors.containsKey(type);
    }

    public ComponentDescriptor createComponentDescriptor(String type, Element config, Plugin plugin)
    {
        // create a new instance of the required descriptor.
        if (!supportsComponentDescriptor(type))
        {
            throw new IllegalArgumentException();
        }

        Class<? extends ComponentDescriptor> descriptorClass = descriptors.get(type);
        ComponentDescriptor descriptor;
        try
        {
            descriptor = objectFactory.buildBean(descriptorClass);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to create descriptor.", e);
        }
        descriptor.setPlugin(plugin);
        descriptor.init(config);
        return descriptor;
    }
}
