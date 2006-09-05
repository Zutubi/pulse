package com.zutubi.plugins.utils;

import java.io.FileFilter;
import java.io.File;

/**
 * <class-comment/>
 */
public class FileOnlyFilter implements FileFilter
{
    public boolean accept(File f)
    {
        return f.isFile();
    }
}