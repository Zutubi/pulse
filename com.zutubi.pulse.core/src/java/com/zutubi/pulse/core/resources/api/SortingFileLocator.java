package com.zutubi.pulse.core.resources.api;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * A file locator that sorts the results of a delegate locator using a
 * comparator.  This is useful if the files should be considered in a specific
 * order.
 */
public class SortingFileLocator implements FileLocator
{
    private FileLocator delegate;
    private Comparator<? super File> comparator;

    /**
     * Creates a locator that will sort the results of the given delegate using
     * the given comparator.
     * 
     * @param delegate   child locator to generate raw results
     * @param comparator comparator used to sort the results
     */
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
