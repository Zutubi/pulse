package com.zutubi.tove.type.record;

import com.zutubi.util.GraphFunction;

import java.util.Collection;
import java.util.Set;

/**
 * Immutable wrapper around a record.
 */
public class ImmutableRecord implements Record
{
    private Record delegate;

    public ImmutableRecord(Record delegate)
    {
        this.delegate = delegate;
    }

    public String getSymbolicName()
    {
        return delegate.getSymbolicName();
    }

    public String getMeta(String key)
    {
        return delegate.getMeta(key);
    }

    public Object get(String key)
    {
        return delegate.get(key);
    }

    public int size()
    {
        return delegate.size();
    }

    public boolean containsMetaKey(String key)
    {
        return delegate.containsMetaKey(key);
    }

    public boolean containsKey(String key)
    {
        return delegate.containsKey(key);
    }

    public MutableRecord copy(boolean deep, boolean preserveHandles)
    {
        return delegate.copy(deep, true);
    }

    public Set<String> keySet()
    {
        return delegate.keySet();
    }

    public Set<String> metaKeySet()
    {
        return delegate.metaKeySet();
    }

    public Set<String> simpleKeySet()
    {
        return delegate.simpleKeySet();
    }

    public Set<String> nestedKeySet()
    {
        return delegate.nestedKeySet();
    }

    public Collection<Object> values()
    {
        return delegate.values();
    }

    public long getHandle()
    {
        return delegate.getHandle();
    }

    public boolean isPermanent()
    {
        return delegate.isPermanent();
    }

    public boolean isCollection()
    {
        return delegate.isCollection();
    }

    public boolean shallowEquals(Record other)
    {
        return delegate.shallowEquals(other);
    }

    public boolean metaEquals(Record other)
    {
        return delegate.metaEquals(other);
    }

    public boolean simpleEquals(Record other)
    {
        return delegate.simpleEquals(other);
    }

    public void forEach(GraphFunction<Record> f)
    {
        delegate.forEach(f);
    }
}
