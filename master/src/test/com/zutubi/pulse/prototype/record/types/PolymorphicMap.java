package com.zutubi.pulse.prototype.record.types;

import com.zutubi.pulse.prototype.record.SymbolicName;

import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated
 */
@SymbolicName("polymap")
public class PolymorphicMap
{
    private Map<String, ParentType> map = new HashMap<String, ParentType>();

    public PolymorphicMap()
    {
    }

    public ParentType get(String key)
    {
        return map.get(key);
    }

    public ParentType put(String key, ParentType value)
    {
        return map.put(key, value);
    }

    public Map<String, ParentType> getMap()
    {
        return map;
    }

    public void setMap(Map<String, ParentType> map)
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

        PolymorphicMap that = (PolymorphicMap) o;

        return !(map != null ? !map.equals(that.map) : that.map != null);
    }

    public int hashCode()
    {
        return (map != null ? map.hashCode() : 0);
    }
}
