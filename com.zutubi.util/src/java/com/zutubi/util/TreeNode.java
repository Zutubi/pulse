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
    private TreeNode<T> parent;
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
        setParent(this, this.children);
    }

    /**
     * Appends the given node as a child of this node.
     *
     * @param child the child to append
     */
    public void add(TreeNode<T> child)
    {
        child.parent = this;
        children.add(child);
    }

    /**
     * Appends the given nodes as children of this node.
     *
     * @param children the children to append
     */
    public void addAll(Collection<TreeNode<T>> children)
    {
        setParent(this, children);
        this.children.addAll(children);
    }

    /**
     * Removes all children from this node.
     */
    public void clear()
    {
        setParent(null, children);
        children.clear();
    }

    private void setParent(TreeNode<T> parent, Collection<TreeNode<T>> children)
    {
        for (TreeNode<T> child : children)
        {
            child.parent = parent;
        }
    }

    /**
     * Returns true if this node is the root of the tree.  The root
     * of a tree is defined as the node without a parent.
     *
     * @return true if this node is the root, false otherwise.
     */
    public boolean isRoot()
    {
        return this.parent == null;
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
     * Retrieves the parent of this node, or null if it is the root.
     *
     * @return the nodes parent in the tree.
     */
    public TreeNode<T> getParent()
    {
        return this.parent;
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
     * Returns the total number of nodes in this tree, including this one.
     *
     * @return the number of nodes in the tree rooted at this node
     */
    public int size()
    {
        int total = 1;
        for (TreeNode<T> child: children)
        {
            total += child.size();
        }

        return total;
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        TreeNode treeNode = (TreeNode) o;

        if (!children.equals(treeNode.children))
        {
            return false;
        }
        if (data != null ? !data.equals(treeNode.data) : treeNode.data != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + children.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append(data);
        for (TreeNode<T> child: children)
        {
            result.append("\n  ");
            result.append(child.toString().replace("\n", "\n  "));
        }

        return result.toString();
    }
}
