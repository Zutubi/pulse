package com.zutubi.pulse.resources;

import com.zutubi.util.Predicate;

import java.io.File;

/**
 */
public class PlainFileFilteringFileLocator extends FilteringFileLocator
{
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
