package com.zutubi.pulse.core.config;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Collection;

/**
 * A map type used within the configuration system, implementing both Map and
 * Configuration.
 */
public class ConfigurationMap<K, V> extends AbstractConfiguration implements Map<K, V>
{
    private Map<K, V> delegate;

    public ConfigurationMap()
    {
        this(10);
    }

    public ConfigurationMap(int capacity)
    {
        delegate = new LinkedHashMap<K,V>(capacity);
    }

    public int size()
    {
        return delegate.size();
    }

    public boolean isEmpty()
    {
        return delegate.isEmpty();
    }

    public boolean containsKey(Object key)
    {
        return delegate.containsKey(key);
    }

    public boolean containsValue(Object value)
    {
        return delegate.containsValue(value);
    }

    public V get(Object key)
    {
        return delegate.get(key);
    }

    public V put(K key, V value)
    {
        return delegate.put(key, value);
    }

    public V remove(Object key)
    {
        return delegate.remove(key);
    }

    public void putAll(Map<? extends K, ? extends V> t)
    {
        delegate.putAll(t);
    }

    public void clear()
    {
        delegate.clear();
    }

    public Set<K> keySet()
    {
        return delegate.keySet();
    }

    public Collection<V> values()
    {
        return delegate.values();
    }

    public Set<Entry<K, V>> entrySet()
    {
        return delegate.entrySet();
    }
}
