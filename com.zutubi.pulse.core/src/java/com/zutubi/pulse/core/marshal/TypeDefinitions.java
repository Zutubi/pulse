package com.zutubi.pulse.core.marshal;

import com.zutubi.tove.type.CompositeType;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores top-level type definitions for a specific type of tove file.
 */
public class TypeDefinitions
{
    private Map<String, CompositeType> nameToType = new HashMap<String, CompositeType>();
    private Map<CompositeType, String> typeToName = new HashMap<CompositeType, String>();

    public void register(String name, CompositeType type)
    {
        nameToType.put(name, type);
        typeToName.put(type, name);
    }

    public void unregister(String name)
    {
        CompositeType type = nameToType.remove(name);
        if (type != null)
        {
            typeToName.remove(type);
        }
    }

    public boolean hasType(String name)
    {
        return getType(name) != null;
    }

    public CompositeType getType(String name)
    {
        return nameToType.get(name);
    }

    public boolean hasName(CompositeType type)
    {
        return getName(type) != null;
    }

    public String getName(CompositeType compositeType)
    {
        return typeToName.get(compositeType);
    }
}
