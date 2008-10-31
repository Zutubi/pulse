package com.zutubi.pulse.core.resources;

import com.zutubi.pulse.core.config.Resource;

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

    public List<Resource> locate()
    {
        List<Resource> result = new LinkedList<Resource>();
        for (ResourceLocator locator: locators)
        {
            result.addAll(locator.locate());
        }

        return result;
    }
}
