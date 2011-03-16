package com.zutubi.pulse.core.plugins;

import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceLocator;

import java.util.LinkedList;
import java.util.List;

/**
 * Describes a plugged-in resource locator.
 */
public class ResourceLocatorDescriptor
{
    private String name;
    private Class<? extends ResourceLocator> clazz;
    
    public ResourceLocatorDescriptor(String name, Class<? extends ResourceLocator> clazz)
    {
        this.name = name;
        this.clazz = clazz;
    }

    public String getName()
    {
        return name;
    }

    public Class<? extends ResourceLocator> getClazz()
    {
        return clazz;
    }
}
