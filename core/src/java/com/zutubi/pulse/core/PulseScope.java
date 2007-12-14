package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.ResourceProperty;

import java.io.File;
import java.util.*;

/**
 * A scope holds named references and has a parent.  When looking up a
 * reference by name, if it is not found in this scope the lookup is
 * deferred to the parent.
 */
public class PulseScope implements Scope
{
    private static final boolean RETAIN_ENVIRONMENT_CASE = System.getProperty("pulse.retain.environment.case") != null;

    // If you add a field, update copy()
    private PulseScope parent;
    private Map<String, Reference> references = new LinkedHashMap<String, Reference>();
    private List<ReferenceInfo> oldrefs = new LinkedList<ReferenceInfo>();

    public PulseScope()
    {

    }

    public PulseScope(PulseScope parent)
    {
        this.parent = parent;
    }

    public PulseScope createChild()
    {
        return new PulseScope(this);
    }

    public PulseScope getParent()
    {
        return parent;
    }

    public void setParent(PulseScope parent)
    {
        this.parent = parent;
    }

    public PulseScope getRoot()
    {
        if(parent == null)
        {
            return this;
        }

        return parent.getRoot();
    }
    
    public Collection<Reference> getOldrefs()
    {
        List<Reference> result = new ArrayList<Reference>(oldrefs.size());

        if (parent != null)
        {
            result.addAll(parent.getOldrefs());
        }

        for (ReferenceInfo info : oldrefs)
        {
            result.add(info.reference);
        }

        return result;
    }

    public List<Reference> getReferences(Class type)
    {
        List<Reference> result = new ArrayList<Reference>(oldrefs.size());

        if (parent != null)
        {
            result.addAll(parent.getReferences(type));
        }

        for (ReferenceInfo info : oldrefs)
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
        if (name.startsWith("env."))
        {
            if (name.equalsIgnoreCase("env.path"))
            {
                return lookupPath(name);
            }
            else
            {
                return lookupEnvironment(name);
            }
        }

        ReferenceInfo info = directLookup(name, oldrefs);
        if (info != null)
        {
            return info.reference;
        }
        else if (parent != null)
        {
            return parent.getReference(name);
        }

        return null;
    }

    public <T> T getReferenceValue(String name, Class<T> type)
    {
        Reference r = getReference(name);
        if (r == null || !type.isInstance(r.getValue()))
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
        if (info == null || !(info.reference.getValue() instanceof String))
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

        if (value == null)
        {
            ReferenceInfo info = directLookup(name, merged);
            if (info == null)
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

    public void addUnique(Reference reference) throws IllegalArgumentException
    {
        if (directLookup(reference.getName(), oldrefs) != null)
        {
            throw new IllegalArgumentException("'" + reference.getName() + "' is already defined in this scope.");
        }

        add(reference);
    }

    public void add(Reference reference)
    {
        add(new ReferenceInfo(reference));
    }

    private void add(ReferenceInfo info)
    {
        oldrefs.add(0, info);
    }

    public void addAll(Collection<? extends Reference> references)
    {
        for (Reference r : references)
        {
            add(r);
        }
    }

    public void addAllUnique(Collection<? extends Reference> references) throws IllegalArgumentException
    {
        for (Reference r : references)
        {
            addUnique(r);
        }
    }

    public <V extends Reference> void add(Map<String, V> references) throws FileLoadException
    {
        for (Reference v : references.values())
        {
            addUnique(v);
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
        for (ReferenceInfo i : merged)
        {
            if (i.addToEnvironment && i.reference.getValue() != null)
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
        for (ReferenceInfo i : merged)
        {
            if (i.addToPath)
            {
                dirs.add((String) i.reference.getValue());
            }
        }

        return dirs;
    }

    public String getPathPrefix()
    {
        String result = "";

        for (String dir : getPathDirectories())
        {
            result += dir;
            result += File.pathSeparatorChar;
        }

        return result;
    }

    public void add(Collection<ResourceProperty> properties)
    {
        for (ResourceProperty resourceProperty : properties)
        {
            add(resourceProperty);
        }
    }

    public void add(ResourceProperty resourceProperty)
    {
        add(new ReferenceInfo(resourceProperty, this));
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
        if (!RETAIN_ENVIRONMENT_CASE)
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
        if (!caseSensitive)
        {
            name = name.toLowerCase();
        }

        for (ReferenceInfo i : references)
        {
            String referenceName = i.reference.getName();
            if (!caseSensitive)
            {
                referenceName = referenceName.toLowerCase();
            }

            if (referenceName.equals(name))
            {
                return i;
            }
        }

        return null;
    }

    private void merge(List<ReferenceInfo> merged)
    {
        for (ReferenceInfo i : oldrefs)
        {
            if (directLookup(i.reference.getName(), merged) == null)
            {
                merged.add(i);
            }
        }

        if (parent != null)
        {
            parent.merge(merged);
        }
    }

    public PulseScope copyTo(Scope scope)
    {
        PulseScope parentCopy = null;
        if(parent != scope && parent != null)
        {
            parentCopy = parent.copyTo(scope);
        }

        PulseScope copy = new PulseScope(parentCopy);
        copy.oldrefs = new LinkedList<ReferenceInfo>(oldrefs);
        return copy;
    }

    public PulseScope copy()
    {
        PulseScope copy = new PulseScope(parent == null ? null : parent.copy());
        // Assumes reference infos are not mutated in some odd way
        copy.oldrefs = new LinkedList<ReferenceInfo>(oldrefs);
        return copy;
    }

    private static class ReferenceInfo
    {
        private Reference reference;
        private boolean addToPath;
        private boolean addToEnvironment;

        public ReferenceInfo()
        {
            // This constructor is to make hessian happy.
        }

        public ReferenceInfo(Reference reference)
        {
            this.reference = reference;
            addToPath = false;
            addToEnvironment = false;
        }

        public ReferenceInfo(ResourceProperty p, ReferenceMap map)
        {
            String value = p.getValue();
            if (p.getResolveVariables())
            {
                try
                {
                    value = VariableHelper.replaceVariables(p.getValue(), map, true);
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
