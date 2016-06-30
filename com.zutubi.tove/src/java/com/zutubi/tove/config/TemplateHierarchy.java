package com.zutubi.tove.config;

import com.google.common.base.Function;
import com.zutubi.tove.type.record.PathUtils;

import java.util.HashMap;
import java.util.Map;

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
    /**
     * Looking up nodes by id happens frequently, so to make it fast we cache a mapping.
     */
    private Map<String, TemplateNode> nodesById = new HashMap<String, TemplateNode>();

    public TemplateHierarchy(String scope, TemplateNode root)
    {
        this.scope = scope;
        this.root = root;
        if (root != null)
        {
            root.forEachDescendant(new Function<TemplateNode, Boolean>()
            {
                public Boolean apply(TemplateNode input)
                {
                    nodesById.put(input.getId(), input);
                    return true;
                }
            }, false, null);
        }
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
        return nodesById.get(id);
    }
}
