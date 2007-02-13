package com.zutubi.prototype;

import java.util.*;

/**
 *
 *
 */
public class PrototypePath
{
    private static final Set scopes = new HashSet<String>();
    {
        scopes.add("project");
    }

    private String fullPath;

    private String scope;
    private String path;
    private long scopeId;

    private List<String> pathElements;

    public PrototypePath(String fullPath)
    {
        this.fullPath = fullPath;
        this.path = "";
        String sep = "";

        StringTokenizer tokens = new StringTokenizer(fullPath, "/", false);

        String token = tokens.nextToken();
        if (scopes.contains(token))
        {
            scope = token;
            if (tokens.hasMoreTokens())
            {
                scopeId = Long.parseLong(tokens.nextToken());
            }
        }
        else
        {
            path = token;
            sep = "/";
        }

        pathElements = new LinkedList<String>();
        while (tokens.hasMoreTokens())
        {
            String pathElement = tokens.nextToken();
            pathElements.add(pathElement);
            path = path + sep + pathElement;
            sep = "/";
        }
    }

    public boolean hasScope()
    {
        return scope != null;
    }

    public String getScope()
    {
        return scope;
    }

    public String getPath()
    {
        return path;
    }

    public long getScopeId()
    {
        return scopeId;
    }

    public List<String> getPathElements()
    {
        return pathElements;
    }

    public String toString()
    {
        return fullPath;
    }

    public String getBasePath()
    {
        if (hasScope())
        {
            return scope + "/" + scopeId;
        }
        return "";
    }
}
