package com.zutubi.prototype.type.record;

import java.util.Set;
import java.util.Collection;

/**
 *
 *
 */
public class ImmutableRecord implements Record
{
    private MutableRecord delegate;

    public ImmutableRecord(MutableRecord delegate)
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

    public boolean containsKey(String key)
    {
        return delegate.containsKey(key);
    }

    public MutableRecord copy(boolean deep)
    {
        return delegate.copy(deep);
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

    public boolean isCollection()
    {
        return delegate.isCollection();
    }
}
