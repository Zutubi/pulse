package com.zutubi.pulse.core.resources.api;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.Collection;

/**
 * A file locator that takes files located by a child locator and filters out
 * any that do not match a given predicate.
 */
public class FilteringFileLocator implements FileLocator
{
    private FileLocator delegate;
    private Predicate<File> predicate;

    /**
     * Creates a new locator that finds candidates with the given delegate and
     * filters them with the given predicate.
     * 
     * @param delegate  child locator used to find candidate files
     * @param predicate predicate used to filter candidates - only files
     *                  satisfying the predicate are returned from
     *                  {@link #locate()}.
     */
    public FilteringFileLocator(FileLocator delegate, Predicate<File> predicate)
    {
        this.delegate = delegate;
        this.predicate = predicate;
    }

    public Collection<File> locate()
    {
        return Lists.newArrayList(Collections2.filter(delegate.locate(), predicate));
    }
}
