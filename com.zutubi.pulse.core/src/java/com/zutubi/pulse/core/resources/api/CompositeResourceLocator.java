package com.zutubi.pulse.core.resources.api;

import java.util.LinkedList;
import java.util.List;

/**
 * A resource locator that combines the resources of multiple child locators.
 */
public class CompositeResourceLocator implements ResourceLocator
{
    private ResourceLocator[] locators;

    /**
     * Creates a new locator with the given children.
     * 
     * @param locators set of locators to run
     */
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
