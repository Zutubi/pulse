package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.MapOption;
import com.zutubi.prototype.MapOptionProvider;
import com.zutubi.prototype.type.TypeProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * An option provider that provides no options.
 */
public class EmptyOptionProvider extends MapOptionProvider
{
    public MapOption getEmptyOption(Object instance, String parentPath, TypeProperty property)
    {
        return new MapOption("", "");
    }

    public Map<String,String> getMap(Object instance, String path, TypeProperty property)
    {
        return new HashMap<String, String>();
    }
}
