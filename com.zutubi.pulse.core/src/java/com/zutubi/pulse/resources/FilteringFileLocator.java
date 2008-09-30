package com.zutubi.pulse.resources;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.io.File;
import java.util.List;

/**
 */
public class FilteringFileLocator implements FileLocator
{
    private FileLocator delegate;
    private Predicate<File> predicate;

    public FilteringFileLocator(FileLocator delegate, Predicate<File> predicate)
    {
        this.delegate = delegate;
        this.predicate = predicate;
    }

    public List<File> locate()
    {
        return CollectionUtils.filter(delegate.locate(), predicate);
    }
}
