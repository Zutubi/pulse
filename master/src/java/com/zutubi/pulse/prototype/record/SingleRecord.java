package com.zutubi.pulse.prototype.record;

import com.zutubi.pulse.prototype.Scope;

import java.util.Map;

/**
 * <class comment/>
 */
public class SingleRecord implements Record
{
    Scope scope;
    String id;
    Map<String, String> data;

    public Map<String, String> getData()
    {
        return data;
    }

    public String getSymbolicName()
    {
        return null;
    }


    public String get(String name)
    {
        return data.get(name);
    }
}
