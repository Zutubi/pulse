package com.zutubi.pulse.web.fs;

import com.zutubi.pulse.filesystem.FileSystem;
import com.zutubi.pulse.filesystem.local.LocalFileSystem;

/**
 * <class-comment/>
 */
public class BrowseServerListAction extends ListAction
{
    private FileSystem fs;

    public FileSystem getFileSystem()
    {
        if (fs == null)
        {
            fs = new LocalFileSystem();
        }
        return fs;
    }
}
