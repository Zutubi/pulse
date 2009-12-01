package com.zutubi.tove.config;

import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

import java.util.LinkedList;
import java.util.List;

/**
 * A node in a heirarchy of templated configuration.  Stores just enough
 * information to display the hierarchy to the user and retrieve further info
 * from the CTM if necessary.
 */
public class TemplateNode
{
    private TemplateNode parent;
    private List<TemplateNode> children = new LinkedList<TemplateNode>();
    private String path;
    private String id;
    private boolean concrete;

    public TemplateNode(String path, String id, boolean concrete)
    {
        this.concrete = concrete;
        this.path = path;
        this.id = id;
    }

    public TemplateNode getParent()
    {
        return parent;
    }

    private void setParent(TemplateNode parent)
    {
        this.parent = parent;
    }

    public List<TemplateNode> getChildren()
    {
        return children;
    }

    public TemplateNode getChild(final String id)
    {
        return CollectionUtils.find(children, new Predicate<TemplateNode>()
        {
            public boolean satisfied(TemplateNode templateNode)
            {
                return templateNode.getId().equals(id);
            }
        });
    }

    public void addChild(TemplateNode child)
    {
        child.setParent(this);
        children.add(child);
    }

    public String getPath()
    {
        return path;
    }

    public String getId()
    {
        return id;
    }

    public boolean isConcrete()
    {
        return concrete;
    }

    public TemplateNode findNodeById(String id)
    {
        if(this.id.equals(id))
        {
            return this;
        }

        for(TemplateNode child: children)
        {
            TemplateNode node = child.findNodeById(id);
            if(node != null)
            {
                return node;
            }
        }

        return null;
    }

    public String getTemplatePath()
    {
        if(parent == null)
        {
            return id;
        }
        else
        {
            return PathUtils.getPath(parent.getTemplatePath(), id);
        }
    }

    public int getDepth()
    {
        if(parent == null)
        {
            return 0;
        }
        else
        {
            return parent.getDepth() + 1;
        }
    }

    public void forEachAncestor(NodeHandler callback, boolean strict)
    {
        if((strict || callback.handle(this)) && parent != null)
        {
            parent.forEachAncestor(callback, false);
        }
    }

    public void forEachDescendant(NodeHandler callback, boolean strict)
    {
        if(strict || callback.handle(this))
        {
            for(TemplateNode node: children)
            {
                node.forEachDescendant(callback, false);
            }
        }
    }

    public interface NodeHandler
    {
        /**
         * Called with a node to be processed.  May optionally abort
         * processing the descendants of this node.
         *
         * @param node the node to process
         * @return true to process this node's descendants
         */
        boolean handle(TemplateNode node);
    }
}
