package com.zutubi.tove.transaction.inmemory;

import java.util.Map;
import java.util.HashMap;

/**
 * An InMemoryStateWrapper implementation that holds a map.
 *
 * Note that it is only the map structure that is managed by this
 * in memory state wrapper, not the contents of the map.
 *
 * @param <U>   the maps key type.
 * @param <V>   the maps value type.
 */
public class InMemoryMapStateWrapper<U, V> extends InMemoryStateWrapper<Map<U, V>>
{
    public InMemoryMapStateWrapper(Map<U, V> state)
    {
        super(state);
    }

    protected InMemoryStateWrapper<Map<U, V>> copy()
    {
        return new InMemoryMapStateWrapper<U, V>(new HashMap<U, V>(get()));
    }
}
