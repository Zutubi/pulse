package com.zutubi.pulse.core.resources;

import com.zutubi.pulse.core.config.ResourceConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 */
public class CompositeResourceLocator implements ResourceLocator
{
    private ResourceLocator[] locators;

    public CompositeResourceLocator(ResourceLocator... locators)
    {
        this.locators = locators;
    }

    public List<ResourceConfiguration> locate()
    {
        List<ResourceConfiguration> result = new LinkedList<ResourceConfiguration>();
        for (ResourceLocator locator: locators)
        {
            result.addAll(locator.locate());
        }

        return result;
    }
}
