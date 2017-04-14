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

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.GraphFunction;

import java.util.Set;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Convenient abstract base for record implementations.
 */
public abstract class AbstractRecord implements Record
{
    protected static final String SYMBOLIC_NAME_KEY = "symbolicName";
    protected static final String HANDLE_KEY = Configuration.HANDLE_KEY;
    protected static final String PERMANENT_KEY = Configuration.PERMANENT_KEY;
    protected static final long UNDEFINED = 0;

    public long getHandle()
    {
        String idString = getMeta(HANDLE_KEY);
        if (idString != null)
        {
            try
            {
                return Long.parseLong(idString);
            }
            catch (NumberFormatException e)
            {
                // Illegal
            }
        }

        return UNDEFINED;
    }

    public boolean isPermanent()
    {
        return Boolean.valueOf(getMeta(PERMANENT_KEY));
    }

    public boolean isCollection()
    {
        return getSymbolicName() == null;
    }

    public boolean shallowEquals(Record other)
    {
        return other != null && metaEquals(other) && simpleEquals(other);
    }

    public boolean metaEquals(Record other)
    {
        Set<String> metaKeys = metaKeySet();
        Set<String> otherMetaKeys = other.metaKeySet();
        if(!metaKeys.equals(otherMetaKeys))
        {
            return false;
        }

        for(String key: metaKeys)
        {
            if(!getMeta(key).equals(other.getMeta(key)))
            {
                return false;
            }
        }

        return true;
    }

    public boolean simpleEquals(Record other)
    {
        Set<String> simpleKeys = simpleKeySet();
        Set<String> otherSimpleKeys = other.simpleKeySet();
        if(!simpleKeys.equals(otherSimpleKeys))
        {
            return false;
        }

        for(String key: simpleKeys)
        {
            if (!RecordUtils.valuesEqual(get(key), other.get(key)))
            {
                return false;
            }
        }

        return true;
    }

    public Record getPath(String path)
    {
        String[] elements = PathUtils.getPathElements(path);
        return getPath(elements, 0);
    }

    public Record getPath(String[] elements, int index)
    {
        if (index == elements.length)
        {
            return this;
        }
        
        Object value = get(elements[index]);
        if (value != null && value instanceof Record)
        {
            return ((Record) value).getPath(elements, index + 1);
        }
        
        return null;
    }

    public Set<String> simpleKeySet()
    {
        return newHashSet(filter(keySet(), new Predicate<String>()
        {
            public boolean apply(String s)
            {
                return !(get(s) instanceof Record);
            }
        }));
    }

    public Set<String> nestedKeySet()
    {
        return newHashSet(filter(keySet(), new Predicate<String>()
        {
            public boolean apply(String s)
            {
                return get(s) instanceof Record;
            }
        }));
    }

    public boolean equals(Object obj)
    {
        if(obj == null || !(obj instanceof AbstractRecord))
        {
            return false;
        }

        AbstractRecord other = (AbstractRecord) obj;
        if(!Objects.equal(getSymbolicName(), other.getSymbolicName()))
        {
            return false;
        }

        Set<String> keySet = keySet();
        if(keySet.size() != other.keySet().size())
        {
            return false;
        }

        for(String key: keySet)
        {
            if(!RecordUtils.valuesEqual(get(key), other.get(key)))
            {
                return false;
            }
        }

        Set<String> metaKeySet = metaKeySet();
        if(metaKeySet.size() != other.metaKeySet().size())
        {
            return false;
        }

        for(String key: metaKeySet)
        {
            if(!getMeta(key).equals(other.getMeta(key)))
            {
                return false;
            }
        }

        return true;
    }

    public int hashCode()
    {
        int code = keySet().hashCode();
        code = 31 * code + metaKeySet().hashCode();
        code = 31 * code + (getSymbolicName() == null ? 0 : getSymbolicName().hashCode());
        return code;
    }

    public void forEach(GraphFunction<Record> f)
    {
        f.process(this);
        for (String key: nestedKeySet())
        {
            if (f.push(key))
            {
                ((Record) get(key)).forEach(f);
                f.pop();
            }
        }
    }
}
