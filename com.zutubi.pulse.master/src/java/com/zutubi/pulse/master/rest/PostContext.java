package com.zutubi.pulse.master.rest;

import com.zutubi.tove.type.CompositeType;

/**
 * Captures contextual information about a configuration path for the purpose of POST'ing. A POST
 * should be used to insert new items into a collection path, or set a property of composite type
 * that is currently null.
 */
public class PostContext
{
    private String parentPath;
    private String baseName;
    private CompositeType postableType;

    public PostContext(String parentPath, String baseName, CompositeType postableType)
    {
        this.parentPath = parentPath;
        this.baseName = baseName;
        this.postableType = postableType;
    }

    public String getParentPath()
    {
        return parentPath;
    }

    public String getBaseName()
    {
        return baseName;
    }

    public CompositeType getPostableType()
    {
        return postableType;
    }
}
