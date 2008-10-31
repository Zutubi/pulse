package com.zutubi.pulse.master.vfs;

import org.apache.commons.vfs.FileFilter;
import org.apache.commons.vfs.FileSelectInfo;

/**
 * Filters file by a prefix of the file path.
 */
public class FilePrefixFilter implements FileFilter
{
    private String prefix;

    public FilePrefixFilter(String prefix)
    {
        this.prefix = prefix;
    }

    public boolean accept(FileSelectInfo fileSelectInfo)
    {
        return prefix == null || fileSelectInfo.getFile().getName().getPath().startsWith(prefix);
    }
}
