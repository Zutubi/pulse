package com.zutubi.util.io;

import com.google.common.base.Predicate;

import java.io.File;

/**
 * A predicate that will match files with the specified suffix.
 */
public class FileSuffixPredicate implements Predicate<File>
{
    private String[] suffixes;

    public FileSuffixPredicate(String... suffixes)
    {
        this.suffixes = suffixes;
    }

    public boolean apply(File file)
    {
        for (String suffix : suffixes)
        {
            if (file.getName().endsWith(suffix))
            {
                return true;
            }
        }
        return false;
    }
}
