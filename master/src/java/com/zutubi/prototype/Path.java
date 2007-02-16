package com.zutubi.prototype;

import java.util.*;

/**
 *
 *
 */
public class Path
{
    private String path;
    
    private Path parent;

    private static final String PATH_SEPARATOR = "/";

    public Path(String path)
    {
        StringTokenizer tokens = new StringTokenizer(path, PATH_SEPARATOR, false);

        // create parents.
        String token = null;
        while (tokens.hasMoreTokens())
        {
            if (token != null)
            {
                parent = new Path(parent, token);
            }
            token = tokens.nextToken();
        }
        this.path = token;
    }

    public Path()
    {
    }

    public Path(Path parent, String pathElement)
    {
        this.path = pathElement;
        this.parent = parent;
    }

    public Path getParent()
    {
        return parent;
    }

    public String getPath()
    {
        if (parent != null)
        {
            return parent.getPath() + PATH_SEPARATOR + path;
        }
        if (path == null)
        {
            return "";
        }
        return path;
    }

    public String getName()
    {
        return path;
    }

    public List<String> getPathElements()
    {
        List<String> pathElements = new LinkedList<String>();
        if (parent != null)
        {
            pathElements.addAll(parent.getPathElements());
            pathElements.add(path);
            return pathElements;
        }
        if (path == null)
        {
            return pathElements;
        }
        pathElements.add(path);
        return pathElements;
    }

    public String toString()
    {
        return getPath();
    }
}
