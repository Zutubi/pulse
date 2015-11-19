package com.zutubi.pulse.master.tove.config;

import com.zutubi.pulse.master.tove.handler.FormContext;
import com.zutubi.pulse.master.tove.handler.MapOptionProvider;
import com.zutubi.tove.type.TypeProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * An option provider that provides no options.
 */
public class EmptyOptionProvider extends MapOptionProvider
{
    public Option getEmptyOption(TypeProperty property, FormContext context)
    {
        return new Option("", "");
    }

    public Map<String,String> getMap(TypeProperty property, FormContext context)
    {
        return new HashMap<>();
    }
}
