package com.zutubi.prototype.config;

import com.zutubi.prototype.type.record.PathUtils;

/**
 * A heirarchy of template configuration meta-data, used to display the
 * template tree and retrieve further information if required.
 */
public class TemplateHierarchy
{
    private String scope;
    private TemplateNode root;

    public TemplateHierarchy(String scope, TemplateNode root)
    {
        this.scope = scope;
        this.root = root;
    }

    public String getScope()
    {
        return scope;
    }

    public TemplateNode getRoot()
    {
        return root;
    }

    public TemplateNode getNode(String path)
    {
        TemplateNode current = null;

        String[] elements = PathUtils.getPathElements(path);
        if(elements.length > 0 && elements[0].equals(root.getId()))
        {
            current = root;
            for(int i = 1; current != null && i < elements.length; i++)
            {
                current = current.getChild(elements[i]);
            }
        }

        return current;
    }
}
