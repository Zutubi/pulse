package com.zutubi.util.io;

import com.google.common.base.Predicate;

import java.io.File;

/**
 * This predicate is only satisfied by files.
 *
 * @see java.io.File#isFile()
 */
public class IsFilePredicate implements Predicate<File>
{
    public boolean apply(File file)
    {
        return file.isFile();
    }
}
