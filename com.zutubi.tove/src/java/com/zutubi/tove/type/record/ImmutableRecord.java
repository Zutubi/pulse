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

    public Record getPath(String path)
    {
        return delegate.getPath(path);
    }

    public Record getPath(String[] elements, int index)
    {
        return delegate.getPath(elements, index);
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ImmutableRecord that = (ImmutableRecord) o;

        if (delegate != null ? !delegate.equals(that.delegate) : that.delegate != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return delegate != null ? delegate.hashCode() : 0;
    }
}
