package com.zutubi.tove.config;

import java.util.List;

/**
 * A node in a hierarchy of templated configuration.  Stores just enough
 * information to display the hierarchy to the user and retrieve further info
 * from the CTM if necessary.
 * <p/>
 * This interface presents an immutable view of the underlying implementation -
 * nodes should not be modified outside of the hierarchy refreshing.
 */
public interface TemplateNode
{
    TemplateNode getParent();

    List<TemplateNode> getChildren();

    TemplateNode getChild(String id);

    String getPath();

    String getId();

    boolean isConcrete();

    TemplateNode findNodeById(String id);

    String getTemplatePath();

    int getDepth();

    void forEachAncestor(NodeHandler callback, boolean strict);

    void forEachDescendant(NodeHandler callback, boolean strict);

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
