package com.zutubi.pulse.core.resources;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class SortingFileLocator implements FileLocator
{
    private FileLocator delegate;
    private Comparator<? super File> comparator;

    public SortingFileLocator(FileLocator delegate, Comparator<? super File> comparator)
    {
        this.delegate = delegate;
        this.comparator = comparator;
    }

    public List<File> locate()
    {
        List<File> result = new LinkedList<File>(delegate.locate());
        Collections.sort(result, comparator);
        return result;
    }
}
