package com.zutubi.pulse.core;

import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.FileSystemUtils;

import java.io.InputStream;

/**
 * Resolves files relative to another path (unless, of course, the path is
 * absolute).
 */
public class RelativeFileResolver implements FileResolver
{
    private String basePath;
    private FileResolver delegate;

    public RelativeFileResolver(String filePath, FileResolver delegate)
    {
        if (filePath != null)
        {
            this.basePath = FileSystemUtils.appendAndCanonicalise(null, PathUtils.getParentPath(filePath));
        }
        this.delegate = delegate;
    }

    public InputStream resolve(String path) throws Exception
    {
        return delegate.resolve(FileSystemUtils.appendAndCanonicalise(basePath, path));
    }
}
