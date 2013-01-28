package com.zutubi.util.io;

import com.google.common.base.Predicate;

import java.io.File;

/**
 * This predicate is only satisfied by directories.
 *
 * @see java.io.File#isDirectory()
 */
public class IsDirectoryPredicate implements Predicate<File>
{
    public boolean apply(File file)
    {
        return file.isDirectory();
    }
}
