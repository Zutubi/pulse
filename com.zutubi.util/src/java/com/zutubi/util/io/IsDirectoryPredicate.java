package com.zutubi.util.io;

import com.zutubi.util.Predicate;

import java.io.File;

/**
 * This predicate is only satisfied by directories.
 *
 * @see java.io.File#isDirectory()
 */
public class IsDirectoryPredicate implements Predicate<File>
{
    public boolean satisfied(File file)
    {
        return file.isDirectory();
    }
}
