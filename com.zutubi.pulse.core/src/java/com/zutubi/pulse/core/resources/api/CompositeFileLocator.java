package com.zutubi.pulse.core.resources.api;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * A file locator that combines the results of multiple child locators.
 */
public class CompositeFileLocator implements FileLocator
{
    private FileLocator[] locators;

    /**
     * Creates a locator with the given children.
     * 
     * @param locators child locators to collect results from
     */
    public CompositeFileLocator(FileLocator... locators)
    {
        this.locators = locators;
    }

    public List<File> locate()
    {
        List<File> result = new LinkedList<File>();
        for (FileLocator locator: locators)
        {
            result.addAll(locator.locate());
        }

        return result;
    }
}
