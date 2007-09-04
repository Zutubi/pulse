package com.zutubi.prototype.type.record;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simple record that holds key:value data, along with meta data.
 */
public class MutableRecordImpl extends AbstractMutableRecord
{
    private Map<String, String> meta = new HashMap<String, String>();

    private Map<String, Object> data = new HashMap<String, Object>();

    private static final String SYMBOLIC_NAME = "symbolicName";

    public void setSymbolicName(String name)
    {
        meta.put(SYMBOLIC_NAME, name);
    }

    public String getSymbolicName()
    {
        return meta.get(SYMBOLIC_NAME);
    }

    public void putMeta(String key, String value)
    {
        meta.put(key, value);
    }

    public String removeMeta(String key)
    {
        return meta.remove(key);
    }

    public String getMeta(String key)
    {
        return meta.get(key);
    }

    public Object put(String key, Object value)
    {
        return data.put(key, value);
    }

    public int size()
    {
        return data.size();
    }

    public Set<String> keySet()
    {
        return data.keySet();
    }

    public Set<String> metaKeySet()
    {
        return meta.keySet();
    }

    public boolean containsKey(String key)
    {
        return data.containsKey(key);
    }

    public Object get(String key)
    {
        return data.get(key);
    }

    public Object remove(String key)
    {
        return data.remove(key);
    }

    public void clear()
    {
        meta.clear();
        data.clear();
    }

    public MutableRecord copy(boolean deep)
    {
        MutableRecordImpl clone = new MutableRecordImpl();

        for (Map.Entry<String, String> entry : meta.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            clone.putMeta(key, value);
        }

        for (Map.Entry<String, Object> entry : data.entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (deep && value instanceof Record)
            {
                value = ((Record) value).copy(deep);
            }
            clone.put(key, value);
        }
        return clone;
    }

    public void update(Record record)
    {
        // take the new primitive data from the record.
        for (String key : record.simpleKeySet())
        {
            data.put(key, record.get(key));
        }
    }

    public Collection<Object> values()
    {
        return data.values();
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

        MutableRecordImpl that = (MutableRecordImpl) o;

        if (data != null ? !data.equals(that.data) : that.data != null)
        {
            return false;
        }

        return !(meta != null ? !meta.equals(that.meta) : that.meta != null);
    }

    public int hashCode()
    {
        int result;
        result = (meta != null ? meta.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

    public void merge(MutableRecordImpl b)
    {
        for (String key : b.keySet())
        {
            Object value = b.get(key);
            
            if (value instanceof Record)
            {
                MutableRecordImpl record = (MutableRecordImpl) value;

                // a) do we have a child record to merge with? if yes, nested merge, if no, clone and set.
                if (this.get(key) instanceof Record)
                {
                    ((MutableRecordImpl)this.get(key)).merge(record);
                }
                else
                {
                    this.put(key, record.copy(true));
                }
            }
            else
            {
                this.put(key, value);
            }
        }
    }

    public MutableRecordImpl diff(MutableRecordImpl b)
    {
        MutableRecordImpl diff = new MutableRecordImpl();
        diff(b, diff);
        return diff;
    }

    private void diff(MutableRecordImpl other, MutableRecordImpl diff)
    {
        // record the diff between this and other in the diff.
        for (String key : other.keySet())
        {
            Object value = other.get(key);
            if (containsKey(key))
            {
                if (value instanceof Record)
                {
                    MutableRecordImpl nestedDiff = new MutableRecordImpl();
                    diff.put(key, nestedDiff);
                    diff((MutableRecordImpl)value, nestedDiff);
                }
                else
                {
                    Object ourValue = get(key);
                    if (!ourValue.equals(value))
                    {
                        diff.put(key, value);
                    }
                }
            }
            else
            {
                // need to copy other if it is a record.
                if (value instanceof Record)
                {
                    diff.put(key, ((Record)value).copy(true));
                }
                else
                {
                    diff.put(key, value);
                }
            }

        }
    }
}
