package com.zutubi.pulse.vfs;

import org.apache.commons.vfs.FileFilter;
import org.apache.commons.vfs.FileSelectInfo;

/**
 * A file filter that accepts files only if they are accepted by all child
 * filters.
 */
public class CompoundFileFilter implements FileFilter
{
    private FileFilter[] children;

    public CompoundFileFilter(FileFilter... children)
    {
        this.children = children;
    }

    public boolean accept(FileSelectInfo fileSelectInfo)
    {
        for(FileFilter f: children)
        {
            if(!f.accept(fileSelectInfo))
            {
                return false;
            }
        }

        return true;
    }
}
