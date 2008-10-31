package com.zutubi.pulse.core.engine.api;

import java.util.Collection;
import java.util.HashMap;

public class HashReferenceMap implements ReferenceMap
{
    private HashMap<String, Reference> references = new HashMap<String, Reference>();

    public boolean containsReference(String name)
    {
        return references.containsKey(name);
    }

    public Reference getReference(String name)
    {
        return references.get(name);
    }

    public Collection<Reference> getReferences()
    {
        return references.values();
    }

    public void add(Reference reference)
    {
        references.put(reference.getName(), reference);
    }

    public void addAll(Collection<? extends Reference> references)
    {
        for (Reference r : references)
        {
            add(r);
        }
    }
}
