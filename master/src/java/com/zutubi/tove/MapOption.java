package com.zutubi.tove;

import java.util.Map;

/**
 * A simple implementation of a map entry that can be used as an option for
 * map option providers.
 *
 * @see MapOptionProvider
 */
public class MapOption implements Map.Entry<String, String>
{
    private String key;
    private String value;

    public MapOption(String key, String value)
    {
        this.key = key;
        this.value = value;
    }

    public String getKey()
    {
        return key;
    }

    public String getValue()
    {
        return value;
    }

    public String setValue(String value)
    {
        String oldValue = this.value;
        this.value = value;
        return oldValue;
    }
}
