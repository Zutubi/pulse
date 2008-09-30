package com.zutubi.pulse.resources;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class ReversingFileLocator implements FileLocator
{
    private FileLocator delegate;

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
