/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
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

    public T getData()
    {
        return data;
    }

    public void setData(T data)
    {
        this.data = data;
    }

    public List<TreeNode<T>> getChildren()
    {
        return children;
    }

    public Iterator<TreeNode<T>> iterator()
    {
        return children.iterator();
    }
}
