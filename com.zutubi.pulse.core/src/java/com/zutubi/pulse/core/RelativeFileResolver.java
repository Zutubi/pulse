package com.zutubi.pulse.core;

import com.zutubi.tove.type.record.PathUtils;

import java.io.InputStream;

/**
 * Resolves files relative to another path (unless, of course, the path is
 * absolute).
 */
public class RelativeFileResolver implements FileResolver
{
    private static final String THIS_DIRECTORY = ".";
    private static final String PARENT_DIRECTORY = "..";

    private String basePath;
    private FileResolver delegate;

    public RelativeFileResolver(String filePath, FileResolver delegate)
    {
        if (filePath != null)
        {
            this.basePath = appendAndCanonicalise(null, PathUtils.getParentPath(filePath));
        }
        this.delegate = delegate;
    }

    public InputStream resolve(String path) throws Exception
    {
        return delegate.resolve(appendAndCanonicalise(basePath, path));
    }

    private String appendAndCanonicalise(String basePath, String path)
    {
        if (path != null && path.startsWith(PathUtils.SEPARATOR))
        {
            return path;
        }
        else
        {
            String result = basePath;
            if (path != null)
            {
                for (String element: PathUtils.getPathElements(path))
                {
                    if (element.equals(THIS_DIRECTORY))
                    {
                        // Skip.
                    }
                    else if (element.equals(PARENT_DIRECTORY))
                    {
                        if (result != null)
                        {
                            result = PathUtils.getParentPath(result);
                        }
                    }
                    else
                    {
                        if (result == null)
                        {
                            result = element;
                        }
                        else
                        {
                            result = PathUtils.getPath(result, element);
                        }
                    }
                }
            }

            return result;
        }
    }
}
