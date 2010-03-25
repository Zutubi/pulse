package com.zutubi.pulse.servercore.filesystem;

import com.zutubi.util.Mapping;

/**
 * A mapping from a file to a file info object.
 */
public class ToFileInfoMapping implements Mapping<java.io.File, FileInfo>
{
    public FileInfo map(java.io.File file)
    {
        return new FileInfo(file);
    }
}
