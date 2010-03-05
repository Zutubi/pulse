package com.zutubi.tove.config;

import com.zutubi.tove.type.record.PathUtils;

/**
 * A hierarchy of template configuration meta-data, used to display the
 * template tree and retrieve further information if required.
 * <p/>
 * This class is designed to be immutable (as indeed are the nodes).
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

    public TemplateNode getNodeByTemplatePath(String path)
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

    public TemplateNode getNodeById(String id)
    {
        return root.findNodeById(id);
    }
}
