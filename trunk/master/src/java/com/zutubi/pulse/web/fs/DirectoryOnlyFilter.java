package com.zutubi.pulse.web.fs;

import java.io.FileFilter;
import java.io.File;

/**
 * <class-comment/>
 */
public class DirectoryOnlyFilter implements FileFilter
{
    public boolean accept(File f)
    {
        return f.isDirectory();
    }
}
