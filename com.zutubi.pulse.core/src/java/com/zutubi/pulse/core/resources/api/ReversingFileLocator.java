package com.zutubi.pulse.core.resources.api;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A file locator that reverses the results of another locator.  This can be
 * useful to consider candidate paths in a different order.
 */
public class ReversingFileLocator implements FileLocator
{
    private FileLocator delegate;

    /**
     * Creates a locator that will reverse the results of the given delegate.
     * 
     * @param delegate delegate used to locate files
     */
    public ReversingFileLocator(FileLocator delegate)
    {
        this.delegate = delegate;
    }

    public List<File> locate()
    {
        List<File> result = new LinkedList<File>(delegate.locate());
        Collections.reverse(result);
        return result;
    }
}
