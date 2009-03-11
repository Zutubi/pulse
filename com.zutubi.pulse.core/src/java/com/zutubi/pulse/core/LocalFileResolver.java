package com.zutubi.pulse.core;

import com.zutubi.tove.type.record.PathUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * A resolver that finds files on the local file system.
 */
public class LocalFileResolver implements FileResolver
{
    private File root;

    public LocalFileResolver(File root)
    {
        this.root = root;
    }

    public InputStream resolve(String path) throws Exception
    {
        if (path.startsWith(PathUtils.SEPARATOR))
        {
            path = path.substring(PathUtils.SEPARATOR.length());
        }
        
        return new FileInputStream(new File(root, path));
    }
}
