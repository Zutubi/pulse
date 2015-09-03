package com.zutubi.tove.config;

import com.google.common.base.Function;

import java.util.Comparator;
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
    /**
     * Returns the parent of this node, if any.
     * 
     * @return the parent of this node, null if this is the hierarchy root
     */
    TemplateNode getParent();

    /**
     * Returns a list of all children of this node.
     * 
     * @return an unmodifiable list of this node's children
     */
    List<TemplateNode> getChildren();

    /**
     * Returns the direct child of this node with the given id, if such a child
     * exists.
     *  
     * @param id the id of the child to find
     * @return the child with the given id, or null if none is found
     */
    TemplateNode getChild(String id);

    /**
     * Returns the path of the configuration represented by this node in the
     * configuration system.  This path is independent of the template
     * hierarchy.
     *  
     * @return the path of the configuration represented by this node
     * 
     * @see #getTemplatePath() 
     */
    String getPath();

    /**
     * Returns a unique name for this node in the hierarchy.
     * 
     * @return a unique name for this node
     */
    String getId();

    /**
     * Returns true if the configuration represented by this node is concrete,
     * false if it is a template.
     * 
     * @return true if the configuration represented by this node is concrete
     */
    boolean isConcrete();

    /**
     * Finds and returns the node with the given id, starting by trying this
     * node followed by a depth-first search of all descendants.
     *  
     * @param id identifier of the node to find
     * @return the non-strict descendant node with the given id, or null if no
     *         such node is found
     */
    TemplateNode findNodeById(String id);

    /**
     * Returns the path of this node from the hierarchy root.  The path is
     * composed of node ids, separated by forward slashes.
     * 
     * @return the path of this node in the hierarchy
     * 
     * @see #getPath() 
     */
    String getTemplatePath();

    /**
     * Returns the distance of this node from the root.  That is, the root has
     * depth 0, its direct children depth 1, their direct children depth 2 and
     * so on.
     *  
     * @return the depth of this node measured from the hierarchy root
     */
    int getDepth();

    /**
     * Walks up the hierarchy from this node, executing the given callback on
     * each node as it goes.  If the callback returns false, the walk is
     * stopped at this node.
     * 
     * @param callback the callback to execute on each node
     * @param strict   if true, this node is not included in the walk
     */
    void forEachAncestor(Function<TemplateNode, Boolean>  callback, boolean strict);

    /**
     * Walks the hierarchy depth-first from this node, executing the given callback on
     * each node followed by it children.  If the callback returns false, the walk is
     * stopped at this node.
     * 
     * @param callback   the callback to execute on each node
     * @param strict     if true, this node is not included in the walk
     * @param comparator if not null, a comparator that will be used to order
     *                   children before walking them
     */
    void forEachDescendant(Function<TemplateNode, Boolean> callback, boolean strict, Comparator<TemplateNode> comparator);
}
