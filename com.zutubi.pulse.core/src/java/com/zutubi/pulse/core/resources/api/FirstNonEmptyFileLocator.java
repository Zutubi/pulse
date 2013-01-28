package com.zutubi.pulse.core.resources.api;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * A file locator that tries child locators in order and returns the results of
 * the first child to find any files.  If no child finds any files, the empty
 * list is returned.
 */
public class FirstNonEmptyFileLocator implements FileLocator
{
    private FileLocator[] delegates;

    /**
     * Creates a locator that will try the given delegates in order until one
     * returns a non-empty list.
     * 
     * @param delegates delegates to try, in order
     */
    public FirstNonEmptyFileLocator(FileLocator... delegates)
    {
        this.delegates = delegates;
    }

    public Collection<File> locate()
    {
        for (FileLocator delegate: delegates)
        {
            Collection<File> results = delegate.locate();
            if (results.size() > 0)
            {
                return results;
            }
        }
        
        return Collections.emptyList();
    }
}
