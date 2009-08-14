package com.zutubi.util;

import com.zutubi.util.math.AggregationFunction;

import java.util.*;

/**
 * Represents a node in a hierarchical structure.  Nodes can hold an arbitrary
 * piece of data.
 */
public class TreeNode<T> implements Iterable<TreeNode<T>>
{
    private T data;
    private List<TreeNode<T>> children;

    public TreeNode(T data)
    {
        this.data = data;
        children = new LinkedList<TreeNode<T>>();
    }

    public void add(TreeNode<T> child)
    {
        children.add(child);
    }

    public void addAll(Collection<TreeNode<T>> ch)
    {
        children.addAll(ch);
    }

    public T getData()
    {
        return data;
    }

    public void setData(T data)
    {
        this.data = data;
    }

    public boolean isLeaf()
    {
        return children.isEmpty();
    }

    public List<TreeNode<T>> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    public Iterator<TreeNode<T>> iterator()
    {
        return children.iterator();
    }

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

    public void depthFirstWalk(TreeNodeOperation<T> op)
    {
        for (TreeNode<T> child: children)
        {
            child.depthFirstWalk(op);
        }

        op.apply(this);
    }
}
