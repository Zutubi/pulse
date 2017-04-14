/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.tove.config;

import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.Configuration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A map type used within the configuration system, implementing both Map and
 * Configuration.
 */
public class ConfigurationMap<V extends Configuration> extends AbstractConfiguration implements Map<String, V>
{
    private Map<String, V> delegate;

    public ConfigurationMap()
    {
        this(10);
    }

    public ConfigurationMap(int capacity)
    {
        delegate = new LinkedHashMap<String,V>(capacity);
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

    public V put(String key, V value)
    {
        return delegate.put(key, value);
    }

    public V remove(Object key)
    {
        return delegate.remove(key);
    }

    public void putAll(Map<? extends String, ? extends V> t)
    {
        delegate.putAll(t);
    }

    public void clear()
    {
        delegate.clear();
    }

    public Set<String> keySet()
    {
        return delegate.keySet();
    }

    public Collection<V> values()
    {
        return delegate.values();
    }

    public Set<Entry<String, V>> entrySet()
    {
        return delegate.entrySet();
    }
}
