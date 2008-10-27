package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.Reference;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * <class-comment/>
 */
public class SimpleRoot
{
    private List<Property> properties = new LinkedList<Property>();
    private Map<String, Reference> references = new TreeMap<String, Reference>();

    public void addProperty(Property p)
    {
        properties.add(p);
    }

    public void addReference(Reference r)
    {
        references.put(r.getName(), r);
    }

    public void add(Reference r)
    {
        references.put(r.getName(), r);
    }

    public Reference getReference(String name)
    {
        return references.get(name);
    }
}
