/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <class-comment/>
 */
public class Scope
{
    private Scope parent;

    private Map<String, Reference> references = new HashMap<String, Reference>();

    public Scope()
    {

    }

    public Scope(Scope parent)
    {
        this.parent = parent;
    }

    public Scope getParent()
    {
        return parent;
    }

    public boolean containsReference(String name)
    {
        if (references.containsKey(name))
        {
            return true;
        }
        else if (parent != null)
        {
            return parent.containsReference(name);
        }
        return false;
    }

    public Reference getReference(String name)
    {
        if (references.containsKey(name))
        {
            return references.get(name);
        }
        else if (parent != null)
        {
            return parent.getReference(name);
        }
        else
        {
            return null;
        }
    }

    public void setReference(Reference reference) throws FileLoadException
    {
        if (references.containsKey(reference.getName()))
        {
            throw new FileLoadException("'" + reference.getName() + "' is already defined in this scope.");
        }
        references.put(reference.getName(), reference);
    }

    public <V extends Reference> void add(List<V> references) throws FileLoadException
    {
        for (Reference r : references)
        {
            setReference(r);
        }
    }

    public <V extends Reference> void add(Map<String, V> references) throws FileLoadException
    {
        for (Reference v : references.values())
        {
            setReference(v);
        }
    }
}
