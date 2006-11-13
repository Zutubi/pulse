package com.zutubi.pulse.web.fs;

import java.io.FileFilter;
import java.io.File;

/**
 * <class-comment/>
 */
public class FileFilterChain implements FileFilter
{
    private FileFilter[] filters;
    public FileFilterChain(FileFilter... filters)
    {
        if (filters == null)
        {
            filters = new FileFilter[0];
        }
        this.filters = filters;
    }

    public boolean accept(File f)
    {
        for (FileFilter ff : filters)
        {
            if (!ff.accept(f))
            {
                return false;
            }
        }
        return true;
    }
}
