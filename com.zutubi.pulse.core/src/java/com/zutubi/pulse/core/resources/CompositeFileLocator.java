package com.zutubi.pulse.core.resources;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class CompositeFileLocator implements FileLocator
{
    private FileLocator[] locators;

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
