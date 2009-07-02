package com.zutubi.util.io;

import java.io.FileFilter;
import java.io.File;

/**
 * An implementation of the FileFilter interface that accepts only
 * directories.
 */
public class DirectoryFileFilter implements FileFilter
{
    public static final FileFilter INSTANCE = new DirectoryFileFilter();
    
    public boolean accept(File file)
    {
        return file.isDirectory();
    }
}
