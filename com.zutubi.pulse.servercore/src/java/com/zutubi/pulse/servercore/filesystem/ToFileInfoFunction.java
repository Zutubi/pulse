package com.zutubi.pulse.servercore.filesystem;

import com.google.common.base.Function;

/**
 * A mapping from a file to a file info object.
 */
public class ToFileInfoFunction implements Function<java.io.File, FileInfo>
{
    public FileInfo apply(java.io.File file)
    {
        return new FileInfo(file);
    }
}
