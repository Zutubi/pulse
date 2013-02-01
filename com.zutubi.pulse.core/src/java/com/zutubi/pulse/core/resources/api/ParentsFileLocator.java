package com.zutubi.pulse.core.resources.api;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.zutubi.util.CollectionUtils;

import java.io.File;
import java.util.Collection;

/**
 * A file locator takes the output of one locator and returns all non-null
 * parent directories of that output.
 */
public class ParentsFileLocator implements FileLocator
{
    private FileLocator delegate;

    /**
     * Creates a locator that will find parents of the given locator's output.
     * 
     * @param delegate delegate used to find initial output
     */
    public ParentsFileLocator(FileLocator delegate)
    {
        this.delegate = delegate;
    }

    public Collection<File> locate()
    {
        return Lists.newArrayList(Collections2.filter(CollectionUtils.map(delegate.locate(), new Function<File, File>()
        {
            public File apply(File file)
            {
                return file.getParentFile();
            }
        }), Predicates.notNull()));
    }
}
