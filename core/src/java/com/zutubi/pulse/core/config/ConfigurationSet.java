package com.zutubi.pulse.core.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A set type used within the configuration system, implementing both Set and
 * Configuration.
 */
public class ConfigurationSet<V extends Configuration> extends AbstractConfiguration implements Set<V>
{
    private Set<V> delegate;

    public ConfigurationSet()
    {
        delegate = new HashSet<V>();
    }

    public int size()
    {
        return delegate.size();
    }

    public boolean isEmpty()
    {
        return delegate.isEmpty();
    }

    public boolean contains(Object o)
    {
        return delegate.contains(o);
    }

    public Iterator<V> iterator()
    {
        return delegate.iterator();
    }

    public Object[] toArray()
    {
        return delegate.toArray();
    }

    public <T> T[] toArray(T[] a)
    {
        return delegate.toArray(a);
    }

    public boolean add(V v)
    {
        return delegate.add(v);
    }

    public boolean remove(Object o)
    {
        return delegate.remove(o);
    }

    public boolean containsAll(Collection<?> c)
    {
        return delegate.containsAll(c);
    }

    public boolean addAll(Collection<? extends V> c)
    {
        return delegate.addAll(c);
    }

    public boolean retainAll(Collection<?> c)
    {
        return delegate.retainAll(c);
    }

    public boolean removeAll(Collection<?> c)
    {
        return delegate.removeAll(c);
    }

    public void clear()
    {
        delegate.clear();
    }
}
