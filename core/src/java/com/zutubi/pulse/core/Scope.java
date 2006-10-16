package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Property;
import com.zutubi.pulse.core.model.ResourceProperty;

import java.util.*;

/**
 * <class-comment/>
 */
public class Scope
{
    private Scope parent;

    private Map<String, Reference> references = new LinkedHashMap<String, Reference>();
    /**
     * Variables to add to the environment when launching child processes in
     * this scope.
     */
    private Map<String, String> environment = new TreeMap<String, String>();
    /**
     * Directories search before the PATH when launching child processed in this
     * scope.
     */
    private Map<String, String> pathDirectories = new TreeMap<String, String>();

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

    public Map<String, Reference> getReferences()
    {
        return references;
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
        add(reference);
    }

    public void add(Reference reference)
    {
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

    public Map<String, String> getEnvironment()
    {
        Map<String, String> merged = new TreeMap<String, String>();
        if(parent != null)
        {
            merged.putAll(parent.getEnvironment());
        }

        merged.putAll(environment);
        return merged;
    }

    public Map<String, String> getPathDirectories()
    {
        Map<String, String> merged = new TreeMap<String, String>();
        if(parent != null)
        {
            merged.putAll(parent.getPathDirectories());
        }

        merged.putAll(pathDirectories);
        return merged;
    }

    public void add(Collection<ResourceProperty> properties)
    {
        for(ResourceProperty resourceProperty: properties)
        {
            add(resourceProperty);
        }
    }

    public void add(ResourceProperty resourceProperty)
    {
        Property property = new Property(resourceProperty.getName(), resourceProperty.getValue());
        add(property);

        if(resourceProperty.getAddToEnvironment())
        {
            environment.put(property.getName(), property.getValue());
        }

        if(resourceProperty.getAddToPath())
        {
            pathDirectories.put(property.getName(), property.getValue());
        }
    }
}
