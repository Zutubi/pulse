package com.zutubi.pulse.core.resources.api;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.NotNullPredicate;

import java.io.File;
import java.util.List;

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

    public List<File> locate()
    {
        return CollectionUtils.filter(CollectionUtils.map(delegate.locate(), new Mapping<File, File>()
        {
            public File map(File file)
            {
                return file.getParentFile();
            }
        }), new NotNullPredicate<File>());
    }
}
