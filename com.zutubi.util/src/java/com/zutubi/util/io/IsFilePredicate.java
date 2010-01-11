package com.zutubi.util.io;

import com.zutubi.util.Predicate;

import java.io.File;

/**
 * This predicate is only satisfied by files.
 *
 * @see java.io.File#isFile()
 */
public class IsFilePredicate implements Predicate<File>
{
    public boolean satisfied(File file)
    {
        return file.isFile();
    }
}
