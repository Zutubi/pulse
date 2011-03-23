package com.zutubi.util;

import java.util.Iterator;
import java.util.List;

/**
 * An iterator that iterates over a linked list from last to first.
 *
 * @param <T>
 */
public class ReverseListIterator<T> implements Iterable<T>, Iterator<T>
{
    private List<T> l;
    private int index;

    public ReverseListIterator(List<T> l)
    {
        this.l = l;
        this.index =  l.size();
    }

    public boolean hasNext()
    {
        return index != 0;
    }

    public T next()
    {
        return l.get(--index);
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    public Iterator<T> iterator()
    {
        return this;
    }
}
