package com.zutubi.pulse.tove.config;

import com.zutubi.tove.MapOption;
import com.zutubi.tove.MapOptionProvider;
import com.zutubi.tove.type.TypeProperty;

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
