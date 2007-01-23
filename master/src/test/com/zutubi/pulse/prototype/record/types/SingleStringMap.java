package com.zutubi.pulse.prototype.record.types;

import com.zutubi.pulse.prototype.record.SymbolicName;

import java.util.HashMap;
import java.util.Map;

/**
 */
@SymbolicName("singlestringmap")
public class SingleStringMap
{
    private Map<String, SingleString> map = new HashMap<String, SingleString>();

    public SingleStringMap()
    {
    }

    public SingleString get(String key)
    {
        return map.get(key);
    }

    public void put(String key, SingleString value)
    {
        map.put(key, value);
    }

    public Map<String, SingleString> getMap()
    {
        return map;
    }

    public void setMap(Map<String, SingleString> map)
    {
        this.map = map;
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

        SingleStringMap stringMap = (SingleStringMap) o;
        return !(map != null ? !map.equals(stringMap.map) : stringMap.map != null);
    }

    public int hashCode()
    {
        return (map != null ? map.hashCode() : 0);
    }
}
