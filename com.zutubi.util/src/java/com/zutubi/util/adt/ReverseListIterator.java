package com.zutubi.util.adt;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * An iterator that iterates over a linked list from last to first.
 *
 * @param <T> type of the items in the list
 */
public class ReverseListIterator<T> implements Iterator<T>
{
    private ListIterator<T> it;

    public ReverseListIterator(List<T> l)
    {
        it = l.listIterator(l.size());
    }

    public boolean hasNext()
    {
        return it.hasPrevious();
    }

    public T next()
    {
        return it.previous();
    }

    public void remove()
    {
        it.remove();
    }
}
