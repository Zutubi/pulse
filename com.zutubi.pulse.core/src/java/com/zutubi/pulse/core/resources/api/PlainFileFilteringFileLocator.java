package com.zutubi.pulse.core.resources.api;

import com.zutubi.util.Predicate;

import java.io.File;

/**
 * A file locator that takes candidates from a delegate locator and filters out
 * any that are not plain files.  Thus {@link #locate()} only returns paths
 * that point to existing plain files.
 */
public class PlainFileFilteringFileLocator extends FilteringFileLocator
{
    /**
     * Creates a plain file locator with the given delegate.
     * 
     * @param delegate locator used to find candidate paths
     */
    public PlainFileFilteringFileLocator(FileLocator delegate)
    {
        super(delegate, new Predicate<File>()
        {
            public boolean satisfied(File file)
            {
                return file.isFile();
            }
        });
    }
}
