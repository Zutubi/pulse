package com.zutubi.pulse.master.tove.config;

import com.zutubi.pulse.master.tove.handler.MapOption;
import com.zutubi.pulse.master.tove.handler.MapOptionProvider;
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
