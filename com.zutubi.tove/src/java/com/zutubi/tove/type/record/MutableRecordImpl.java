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

import java.util.*;

/**
 * Simple record that holds key:value data, along with meta data.
 */
public class MutableRecordImpl extends AbstractMutableRecord 
{
    private Map<String, String> meta = null;

    private Map<String, Object> data = null;

    public void setSymbolicName(String name)
    {
        if (name == null)
        {
            getMeta().remove(SYMBOLIC_NAME_KEY);
        }
        else
        {
            getMeta().put(SYMBOLIC_NAME_KEY, name);
        }
    }

    public String getSymbolicName()
    {
        return getMeta().get(SYMBOLIC_NAME_KEY);
    }

    public void putMeta(String key, String value)
    {
        if (value == null)
        {
            throw new NullPointerException();
        }
        getMeta().put(key, value);
    }

    public String removeMeta(String key)
    {
        return getMeta().remove(key);
    }

    public String getMeta(String key)
    {
        return getMeta().get(key);
    }

    public Object put(String key, Object value)
    {
        if (value == null)
        {
            throw new NullPointerException();
        }
        return getData().put(key, value);
    }

    public int size()
    {
        return getData().size();
    }

    public Set<String> keySet()
    {
        return getData().keySet();
    }

    public Set<String> metaKeySet()
    {
        return getMeta().keySet();
    }

    public boolean containsMetaKey(String key)
    {
        return getMeta().containsKey(key);
    }

    public boolean containsKey(String key)
    {
        return getData().containsKey(key);
    }

    public Object get(String key)
    {
        return getData().get(key);
    }

    public Object remove(String key)
    {
        return getData().remove(key);
    }

    public void clear()
    {
        getMeta().clear();
        getData().clear();
    }

    public MutableRecord copy(boolean deep, boolean preserveHandles)
    {
        MutableRecordImpl clone = new MutableRecordImpl();
        if (meta != null)
        {
            clone.meta = new HashMap<String, String>(meta);
            if (!preserveHandles)
            {
                clone.meta.remove(HANDLE_KEY);
            }
        }

        if (data != null)
        {
            if (deep)
            {
                for (Map.Entry<String, Object> entry : data.entrySet())
                {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof Record)
                    {
                        value = ((Record) value).copy(true, preserveHandles);
                    }
                    clone.put(key, value);
                }
            }
            else
            {
                clone.data = new HashMap<String, Object>(data);
            }
        }

        return clone;
    }

    public void update(Record record, boolean deep, boolean overwriteExisting)
    {
        Map<String, String> meta = getMeta();
        for (String key : record.metaKeySet())
        {
            if (overwriteExisting || !meta.containsKey(key))
            {
                meta.put(key, record.getMeta(key));
            }
        }

        Map<String, Object> data = getData();
        for (String key : record.simpleKeySet())
        {
            if (overwriteExisting || !data.containsKey(key))
            {
                data.put(key, record.get(key));
            }
        }

        if (deep)
        {
            for (String key : record.nestedKeySet())
            {
                Record nested = (Record) record.get(key);
                Object existing = data.get(key);
                if (existing == null)
                {
                    put(key, nested.copy(true, false));
                }
                else if (existing instanceof MutableRecord)
                {
                    ((MutableRecord) existing).update(nested, deep, overwriteExisting);
                }
            }
        }
    }

    public Collection<Object> values()
    {
        return getData().values();
    }

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

        return super.equals(o);
    }

    private Map<String, String> getMeta()
    {
        if (meta == null)
        {
            meta = new HashMap<String, String>();
        }
        return meta;
    }

    private Map<String, Object> getData()
    {
        if (data == null)
        {
            data = new LinkedHashMap<String, Object>();
        }
        return data;
    }

    protected Object clone() throws CloneNotSupportedException
    {
        super.clone();
        return copy(true, true);
    }
}
