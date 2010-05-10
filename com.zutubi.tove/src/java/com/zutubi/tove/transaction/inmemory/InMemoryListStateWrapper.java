package com.zutubi.tove.transaction.inmemory;

import java.util.LinkedList;
import java.util.List;

/**
 * An InMemoryStateWrapper implementation that holds a list.
 *
 * Note that it is only the lists structure that is managed by this
 * in memory state wrapper, not the contents of the list.
 *
 * @param <U>   the type of value held by the list.
 */
public class InMemoryListStateWrapper<U> extends InMemoryStateWrapper<List<U>>
{
    public InMemoryListStateWrapper(List<U> state)
    {
        super(state);
    }

    protected InMemoryStateWrapper<List<U>> copy()
    {
        return new InMemoryListStateWrapper<U>(new LinkedList<U>(get()));
    }
}