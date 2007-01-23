package com.zutubi.pulse.prototype.record.types;

import com.zutubi.pulse.prototype.record.SymbolicName;

import java.util.HashMap;
import java.util.Map;

/**
 */
@SymbolicName("stringmap")
public class StringMap
{
    private Map<String, String> map = new HashMap<String, String>();

    public StringMap()
    {
    }

    public String get(String key)
    {
        return map.get(key);
    }

    public void put(String key, String value)
    {
        map.put(key, value);
    }

    public Map<String, String> getMap()
    {
        return map;
    }

    public void setMap(Map<String, String> map)
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

        StringMap stringMap = (StringMap) o;
        return !(map != null ? !map.equals(stringMap.map) : stringMap.map != null);
    }

    public int hashCode()
    {
        return (map != null ? map.hashCode() : 0);
    }
}
