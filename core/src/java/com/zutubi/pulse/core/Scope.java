package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.ResourceProperty;

import java.io.File;
import java.util.*;

/**
 * A scope holds named references and has a parent.  When looking up a
 * reference by name, if it is not found in this scope the lookup is
 * deferred to the parent.
 */
public class Scope
{
    private static final boolean RETAIN_ENVIRONMENT_CASE = System.getProperty("pulse.retain.environment.case") != null;

    private Scope parent;

    private List<ReferenceInfo> references = new LinkedList<ReferenceInfo>();

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

    public void setParent(Scope parent)
    {
        this.parent = parent;
    }

    public List<Reference> getReferences()
    {
        List<Reference> result = new ArrayList<Reference>(references.size());

        if (parent != null)
        {
            result.addAll(parent.getReferences());
        }

        for(ReferenceInfo info: references)
        {
            result.add(info.reference);
        }

        return result;
    }

    public List<Reference> getReferences(Class type)
    {
        List<Reference> result = new ArrayList<Reference>(references.size());

        if (parent != null)
        {
            result.addAll(parent.getReferences(type));
        }

        for(ReferenceInfo info: references)
        {
            if (type.isInstance(info.reference.getValue()))
            {
                result.add(info.reference);
            }
        }

        return result;
    }

    public boolean containsReference(String name)
    {
        return getReference(name) != null;
    }

    public Reference getReference(String name)
    {
        if(name.startsWith("env."))
        {
            if(name.equalsIgnoreCase("env.path"))
            {
                return lookupPath(name);
            }
            else
            {
                return lookupEnvironment(name);
            }
        }

        ReferenceInfo info = directLookup(name, references);
        if(info != null)
        {
            return info.reference;
        }
        else if(parent != null)
        {
            return parent.getReference(name);
        }

        return null;
    }

    public <T> T getReferenceValue(String name, Class<T> type)
    {
        Reference r = getReference(name);
        if(r == null || !type.isInstance(r.getValue()))
        {
            return null;
        }

        return (T) r.getValue();
    }

    private Reference lookupPath(String name)
    {
        List<ReferenceInfo> merged = new LinkedList<ReferenceInfo>();
        merge(merged);

        ReferenceInfo info = directLookup(name, merged, false);
        String value;
        if(info == null || !(info.reference.getValue() instanceof String))
        {
            value = "";
        }
        else
        {
            value = (String) info.reference.getValue();
        }

        return new Property(name, getPathPrefix() + value);
    }

    private Reference lookupEnvironment(String name)
    {
        List<ReferenceInfo> merged = new LinkedList<ReferenceInfo>();
        merge(merged);

        Map<String, String> env = getEnvironment(merged);
        String var = name.substring(4);
        String value = env.get(var);

        if(value == null)
        {
            ReferenceInfo info = directLookup(name, merged);
            if(info == null)
            {
                return null;
            }
            else
            {
                return info.reference;
            }
        }
        else
        {
            return new Property(name, value);
        }
    }

    public void setReference(Reference reference) throws FileLoadException
    {
        if(directLookup(reference.getName(), references) != null)
        {
            throw new FileLoadException("'" + reference.getName() + "' is already defined in this scope.");
        }

        add(reference);
    }

    public void add(Reference reference)
    {
        add(new ReferenceInfo(reference));
    }

    private void add(ReferenceInfo info)
    {
        references.add(0, info);
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
        List<ReferenceInfo> merged = new LinkedList<ReferenceInfo>();
        merge(merged);

        return getEnvironment(merged);
    }

    private Map<String, String> getEnvironment(List<ReferenceInfo> merged)
    {
        Map<String, String> env = new TreeMap<String, String>();
        for(ReferenceInfo i: merged)
        {
            if(i.addToEnvironment && i.reference.getValue() != null)
            {
                env.put(i.reference.getName(), (String) i.reference.getValue());
            }
        }

        return env;
    }

    public List<String> getPathDirectories()
    {
        List<ReferenceInfo> merged = new LinkedList<ReferenceInfo>();
        merge(merged);

        List<String> dirs = new LinkedList<String>();
        for(ReferenceInfo i: merged)
        {
            if(i.addToPath)
            {
                dirs.add((String) i.reference.getValue());
            }
        }

        return dirs;
    }

    public String getPathPrefix()
    {
        String result = "";

        for(String dir: getPathDirectories())
        {
            result += dir;
            result += File.pathSeparatorChar;
        }

        return result;
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
        add(new ReferenceInfo(resourceProperty));
    }

    /**
     * Adds a special env. property to this scope to store the value of an
     * environment variable.
     *
     * @param name  the name of the environment variable, to which "env." is
     *              prepended to make the property name
     * @param value the value of the variable
     */
    public void addEnvironmentProperty(String name, String value)
    {
        if(!RETAIN_ENVIRONMENT_CASE)
        {
            name = name.toUpperCase();
        }

        add(new Property("env." + name, value));
    }

    private ReferenceInfo directLookup(String name, List<ReferenceInfo> references)
    {
        return directLookup(name, references, true);
    }

    private ReferenceInfo directLookup(String name, List<ReferenceInfo> references, boolean caseSensitive)
    {
        if(!caseSensitive)
        {
            name = name.toLowerCase();
        }

        for(ReferenceInfo i: references)
        {
            String referenceName = i.reference.getName();
            if(!caseSensitive)
            {
                referenceName = referenceName.toLowerCase();
            }

            if(referenceName.equals(name))
            {
                return i;
            }
        }

        return null;
    }

    private void merge(List<ReferenceInfo> merged)
    {
        for(ReferenceInfo i: references)
        {
            if(directLookup(i.reference.getName(), merged) == null)
            {
                merged.add(i);
            }
        }

        if(parent != null)
        {
            parent.merge(merged);
        }
    }

    private class ReferenceInfo
    {
        public Reference reference;
        public boolean addToPath;
        public boolean addToEnvironment;

        public ReferenceInfo(Reference reference)
        {
            this.reference = reference;
            addToPath = false;
            addToEnvironment = false;
        }

        public ReferenceInfo(ResourceProperty p)
        {
            String value = p.getValue();
            if (p.getResolveVariables())
            {
                try
            {
                value = VariableHelper.replaceVariables(p.getValue(), Scope.this, true);
                }
                catch (FileLoadException e)
                {
                    // Just use the unresolved value
                }
            }

            this.reference = new Property(p.getName(), value);
            this.addToPath = p.getAddToPath();
            this.addToEnvironment = p.getAddToEnvironment();
        }

        public String toString()
        {
            return reference.getName() + " -> " + reference.getValue() + " [path: " + addToPath + ", env: " + addToEnvironment + "]";
        }
    }
}
