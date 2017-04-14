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

import java.util.*;

/**
 * A list type used within the configuration system, implementing both List
 * and Configuration.
 */
public class ConfigurationList<T> extends AbstractConfiguration implements List<T>
{
    private List<T> delegate = new LinkedList<T>();

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

    public Iterator<T> iterator()
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

    public boolean add(T o)
    {
        return delegate.add(o);
    }

    public boolean remove(Object o)
    {
        return delegate.remove(o);
    }

    public boolean containsAll(Collection<?> c)
    {
        return delegate.containsAll(c);
    }

    public boolean addAll(Collection<? extends T> c)
    {
        return delegate.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends T> c)
    {
        return delegate.addAll(index, c);
    }

    public boolean removeAll(Collection<?> c)
    {
        return delegate.removeAll(c);
    }

    public boolean retainAll(Collection<?> c)
    {
        return delegate.retainAll(c);
    }

    public void clear()
    {
        delegate.clear();
    }

    public boolean equals(Object o)
    {
        return delegate.equals(o);
    }

    public int hashCode()
    {
        return delegate.hashCode();
    }

    public T get(int index)
    {
        return delegate.get(index);
    }

    public T set(int index, T element)
    {
        return delegate.set(index, element);
    }

    public void add(int index, T element)
    {
        delegate.add(index, element);
    }

    public T remove(int index)
    {
        return delegate.remove(index);
    }

    public int indexOf(Object o)
    {
        return delegate.indexOf(o);
    }

    public int lastIndexOf(Object o)
    {
        return delegate.lastIndexOf(o);
    }

    public ListIterator<T> listIterator()
    {
        return delegate.listIterator();
    }

    public ListIterator<T> listIterator(int index)
    {
        return delegate.listIterator(index);
    }

    public List<T> subList(int fromIndex, int toIndex)
    {
        return delegate.subList(fromIndex, toIndex);
    }
}
