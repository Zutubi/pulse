package com.zutubi.pulse.core;

import java.util.Map;
import java.util.HashMap;

/**
 * <class comment/>
 */
public class ComponentRegistry
{
    private final Map<String, Class> typeDefinitions = new HashMap<String, Class>();

    public void register(String name, Class type)
    {
        typeDefinitions.put(name, type);
    }

    public Map<String, Class> getTypeDefinitions()
    {
        return typeDefinitions;
    }
}
