package com.zutubi.util.io;

import java.io.FileFilter;
import java.io.File;

/**
 * Accepts only files that end with the specified suffix string.
 */
public class FileSuffixFilter implements FileFilter
{
    private String suffix;

    public FileSuffixFilter(String suffix)
    {
        this.suffix = suffix;
    }

    public boolean accept(File pathname)
    {
        return pathname.getName().endsWith(suffix);
    }
}
