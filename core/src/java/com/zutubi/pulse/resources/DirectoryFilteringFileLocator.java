package com.zutubi.pulse.resources;

import com.zutubi.util.Predicate;

import java.io.File;

/**
 */
public class DirectoryFilteringFileLocator extends FilteringFileLocator
{
    public DirectoryFilteringFileLocator(FileLocator delegate)
    {
        super(delegate, new Predicate<File>()
        {
            public boolean satisfied(File file)
            {
                return file.isDirectory();
            }
        });
    }
}
