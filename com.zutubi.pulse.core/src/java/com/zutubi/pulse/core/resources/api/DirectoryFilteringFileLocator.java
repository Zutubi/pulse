package com.zutubi.pulse.core.resources.api;

import com.google.common.base.Predicate;

import java.io.File;

/**
 * A file locator that takes candidates from a delegate locator and filters out
 * any that are not directories.  Thus {@link #locate()} only returns paths
 * that point to existing directories.
 */
public class DirectoryFilteringFileLocator extends FilteringFileLocator
{
    /**
     * Creates a directory locator with the given delegate.
     * 
     * @param delegate locator used to find candidate paths
     */
    public DirectoryFilteringFileLocator(FileLocator delegate)
    {
        super(delegate, new Predicate<File>()
        {
            public boolean apply(File file)
            {
                return file.isDirectory();
            }
        });
    }
}
