package com.zutubi.util;

import com.zutubi.util.math.AggregationFunction;

import static java.util.Arrays.asList;
import java.util.*;

/**
 * Represents a node in a hierarchical structure.  Nodes can hold an arbitrary
 * piece of data.
 */
public class TreeNode<T> implements Iterable<TreeNode<T>>
{
    private T data;
    private List<TreeNode<T>> children;

    /**
     * Creates a tree node with the given children.
     *
     * @param data     data held by this node
     * @param children initial children for this node
     */
    public TreeNode(T data, TreeNode<T>... children)
    {
        this.data = data;
        this.children = new LinkedList<TreeNode<T>>(asList(children));
    }

    /**
     * Appends the given node as a child of this node.
     *
     * @param child the child to append
     */
    public void add(TreeNode<T> child)
    {
        children.add(child);
    }

    /**
     * Appends the given nodes as children of this node.
     *
     * @param ch the children to append
     */
    public void addAll(Collection<TreeNode<T>> ch)
    {
        children.addAll(ch);
    }

    /**
     * Removes all children from this node.
     */
    public void clear()
    {
        children.clear();
    }

    /**
     * Retrieves the data in this node.
     *
     * @return this node's data
     */
    public T getData()
    {
        return data;
    }

    /**
     * Updates the data in this node.
     *
     * @param data the new node data
     */
    public void setData(T data)
    {
        this.data = data;
    }

    /**
     * Indicates if this node is a leaf in its tree (i.e. has no children).
     *
     * @return true if this node has no children
     */
    public boolean isLeaf()
    {
        return children.isEmpty();
    }

    /**
     * Returns an unmodifiable list of all direct children of this node.
     *
     * @return all direct children of this node
     */
    public List<TreeNode<T>> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    /**
     * Returns an iterator over the direct children of this node.
     *
     * @return an iterator over this node's direct children
     */
    public Iterator<TreeNode<T>> iterator()
    {
        return children.iterator();
    }

    /**
     * Indicates the depth of this node, as the maximum length of a path from
     * this to a leaf node.  If this node is a leaf its depth is zero.
     *
     * @return the depth of this node as measure from the furthest leaf
     */
    public int depth()
    {
        if (isLeaf())
        {
            return 0;
        }
        else
        {
            Number maxChildDepth = AggregationFunction.MAX.aggregate(CollectionUtils.map(children, new Mapping<TreeNode<T>, Number>()
            {
                public Number map(TreeNode<T> child)
                {
                    return child.depth();
                }
            }));

            return maxChildDepth.intValue() + 1;
        }
    }

    /**
     * Walks over the tree rooted at this node in depth first order, applying
     * the given operation to each node.  Depth-first order guarantees a node's
     * children are all processed before the node itself is.
     *
     * @param fn the operation to apply to all nodes
     */
    public void depthFirstWalk(UnaryProcedure<TreeNode<T>> fn)
    {
        for (TreeNode<T> child: children)
        {
            child.depthFirstWalk(fn);
        }

        fn.process(this);
    }

    /**
     * Walks over the tree rooted at this node in breadth first order, applying
     * the given operation to each node.  Breadth-first order guarantees all
     * nodes at some depth X are processed before any node at depth X + 1.
     *
     * @param fn the operation to apply to all nodes
     */
    public void breadthFirstWalk(UnaryProcedure<TreeNode<T>> fn)
    {
        Queue<TreeNode<T>> toProcess = new LinkedList<TreeNode<T>>();
        toProcess.offer(this);
        breadthFirstWalk(fn, toProcess);
    }

    private void breadthFirstWalk(UnaryProcedure<TreeNode<T>> fn, Queue<TreeNode<T>> toProcess)
    {
        while (!toProcess.isEmpty())
        {
            TreeNode<T> next = toProcess.remove();
            for (TreeNode<T> child: next.getChildren())
            {
                toProcess.offer(child);
            }

            fn.process(next);
        }
    }

    /**
     * Walks over the tree recursively, removing any nodes that do not satisfy
     * the given predicate from their parent.  Note that the root itself is
     * never filtered out.
     *
     * @param predicate predicate to test which nodes should pass the filter
     */
    public void filteringWalk(Predicate<TreeNode<T>> predicate)
    {
        children = CollectionUtils.filter(children, predicate);
        for (TreeNode<T> child: children)
        {
            child.filteringWalk(predicate);
        }
    }
}
