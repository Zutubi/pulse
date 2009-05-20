package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.Property;
import com.zutubi.pulse.core.engine.api.Reference;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.engine.api.Scope;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;
import com.zutubi.util.TextUtils;

import java.io.File;
import java.util.*;

/**
 * A scope holds named references and has a parent.  When looking up a
 * reference by name, if it is not found in this scope the lookup is
 * deferred to the parent.
 *
 * The PulseScope is an implementation of a scope that recognises 2 special
 * sets of properties known.  The first is environment properties, identified
 * by there env. prefix.  The second are resource properties that can be
 * added to the environment as well as added to a specific environment property
 * env.PATH
 */
public class PulseScope implements Scope
{
    private static final String ENV_PATH = "PATH";

    private static final boolean RETAIN_ENVIRONMENT_CASE = System.getProperty("pulse.retain.environment.case") != null;

    // If you add a field, update copyTo()
    private PulseScope parent;
    // Optional label that can be used to marking and finding specific scopes
    private String label;
    private Map<String, ReferenceInfo> references = new LinkedHashMap<String, ReferenceInfo>();

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

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public PulseScope getRoot()
    {
        if(parent == null)
        {
            return this;
        }

        return parent.getRoot();
    }

    public PulseScope getAncestor(String label)
    {
        if(label.equals(this.label))
        {
            return this;
        }

        if(parent != null)
        {
            return parent.getAncestor(label);
        }

        return null;
    }

    private Map<String, ReferenceInfo> merge()
    {
        Map<String, ReferenceInfo> merged = new LinkedHashMap<String, ReferenceInfo>();
        if(parent != null)
        {
            merged.putAll(parent.merge());
        }

        // We do not use putAll here as the LinkedHashMap does not preserve
        // ordering for re-entries (i.e. putting on top of an existing key)
        // whereas we want to preserve ordering.  Hence re-entries are
        // avoided by removing existing keys.
        for(Map.Entry<String, ReferenceInfo> entry: references.entrySet())
        {
            if(merged.containsKey(entry.getKey()))
            {
                merged.remove(entry.getKey());
            }

            merged.put(entry.getKey(), entry.getValue());
        }

        return merged;
    }
    
    public Collection<Reference> getReferences()
    {
        return CollectionUtils.map(merge().values(), new Mapping<ReferenceInfo, Reference>()
        {
            public Reference map(ReferenceInfo referenceInfo)
            {
                return referenceInfo.reference;
            }
        });
    }

    private List<Reference> getReferencesSatisfyingPredicate(Predicate<ReferenceInfo> p)
    {
        return CollectionUtils.map(CollectionUtils.filter(merge().values(), p), new Mapping<ReferenceInfo, Reference>()
        {
            public Reference map(ReferenceInfo referenceInfo)
            {
                return referenceInfo.reference;
            }
        });
    }

    public List<Reference> getReferences(final Class type)
    {
        return getReferencesSatisfyingPredicate(new Predicate<ReferenceInfo>()
        {
            public boolean satisfied(ReferenceInfo referenceInfo)
            {
                return type.isInstance(referenceInfo.reference.getValue());
            }
        });
    }

    public boolean containsReference(String name)
    {
        return getReference(name) != null;
    }

    public Reference getReference(String name)
    {
        ReferenceInfo info = merge().get(name);
        Reference result = info == null ? null : info.reference;

        if(name.startsWith("env."))
        {
            // Lets see if there is a property value that overrides the default environment variable.
            // This override is the behaviour we see when actually running a Pulse command.
            String envName = name.substring(4);
            Map<String, String> environment = getEnvironment();
            String value = environment.get(envName);
            if(value != null)
            {
                result = new Property(name, value);
            }

            // Special case PATH to add the prefix
            if(envName.toUpperCase(Locale.US).equals(ENV_PATH))
            {
                String pathPrefix = getPathPrefix();
                if(TextUtils.stringSet(pathPrefix))
                {
                    if(result == null)
                    {
                        result = new Property(name, pathPrefix.substring(0, pathPrefix.length() - 1));
                    }
                    else if((result.getValue() instanceof String))
                    {
                        result = new Property(name, pathPrefix + result.getValue());
                    }
                }
            }
        }

        return result;
    }

    public <T> T getReferenceValue(String name, Class<T> type)
    {
        Reference r = getReference(name);
        if (r == null || !type.isInstance(r.getValue()))
        {
            return null;
        }

        return type.cast(r.getValue());
    }

    public void addUnique(Reference reference) throws IllegalArgumentException
    {
        if (references.containsKey(reference.getName()))
        {
            throw new IllegalArgumentException("'" + reference.getName() + "' is already defined in this scope.");
        }

        add(reference);
    }

    public void add(PulseScope other)
    {
        for (ReferenceInfo info: other.merge().values())
        {
            references.put(info.reference.getName(), info);
        }
    }

    public void add(Reference reference)
    {
        references.put(reference.getName(), new ReferenceInfo(reference));
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

    /**
     * Returns an environment map that includes all properties marked add to
     * environment.  Note that changes to the PATH from add to path
     * properties are not taken into account.
     *
     * @see #applyEnvironment(java.util.Map)
     *
     * @return the environment defined by add to environment properties in
     *         this scope
     */
    public Map<String, String> getEnvironment()
    {
        Collection<Reference> references = getReferencesSatisfyingPredicate(new Predicate<ReferenceInfo>()
        {
            public boolean satisfied(ReferenceInfo referenceInfo)
            {
                return referenceInfo.addToEnvironment && (referenceInfo.reference.getValue() instanceof String);
            }
        });

        Map<String, String> result = new HashMap<String, String>(references.size());
        for(Reference r: references)
        {
            result.put(r.getName(), (String) r.getValue());
        }

        return result;
    }

    /**
     * Applies the environment defined within this scope to an existing
     * environment.  Properties marked as add to environment are added, and
     * the prefix defined by add to path properties is added to the PATH.
     *
     * @param existingEnvironment existing environment to add to
     */
    public void applyEnvironment(Map<String, String> existingEnvironment)
    {
        existingEnvironment.putAll(getEnvironment());

        String pathPrefix = getPathPrefix();
        if(TextUtils.stringSet(pathPrefix))
        {
            String pathKey = null;
            for(String key: existingEnvironment.keySet())
            {
                if(key.toUpperCase(Locale.US).equals(ENV_PATH))
                {
                    pathKey = key;
                    break;
                }
            }

            if(pathKey == null)
            {
                existingEnvironment.put(ENV_PATH, pathPrefix.substring(0, pathPrefix.length() - 1));
            }
            else
            {
                existingEnvironment.put(pathKey, pathPrefix + existingEnvironment.get(pathKey));
            }
        }
    }

    public List<String> getPathDirectories()
    {
        Collection<Reference> references = getReferencesSatisfyingPredicate(new Predicate<ReferenceInfo>()
        {
            public boolean satisfied(ReferenceInfo referenceInfo)
            {
                return referenceInfo.addToPath && referenceInfo.reference.getValue() instanceof String;
            }
        });
        
        List<String> result = CollectionUtils.map(references, new Mapping<Reference, String>()
        {
            public String map(Reference reference)
            {
                return (String) reference.getValue();
            }
        });

        // Reverse so the most local and recently added values end up earlier
        // in the path.
        Collections.reverse(result);
        return result;
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

    public void add(Collection<? extends ResourceProperty> properties)
    {
        for (ResourceProperty resourceProperty : properties)
        {
            add(resourceProperty);
        }
    }

    public void add(ResourceProperty resourceProperty)
    {
        String value = resourceProperty.getValue();
        if(resourceProperty.getResolveVariables())
        {
            try
            {
                value = ReferenceResolver.resolveReferences(value, this, ReferenceResolver.ResolutionStrategy.RESOLVE_NON_STRICT);
            }
            catch (ResolutionException e)
            {
                // Just use unresolved value.
            }
        }

        String name = resourceProperty.getName();
        references.put(name, new ReferenceInfo(new Property(name, value), resourceProperty.getAddToEnvironment(), resourceProperty.getAddToPath()));
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

    public PulseScope copyTo(Scope scope)
    {
        PulseScope parentCopy = null;
        if(parent != scope && parent != null)
        {
            parentCopy = parent.copyTo(scope);
        }

        PulseScope copy = new PulseScope(parentCopy);
        copy.label = label;
        copy.references = new LinkedHashMap<String, ReferenceInfo>(references);
        return copy;
    }

    public PulseScope copy()
    {
        return copyTo(null);
    }

    private static class ReferenceInfo
    {
        private Reference reference;
        private boolean addToEnvironment = false;
        private boolean addToPath = false;

        public ReferenceInfo(Reference reference)
        {
            this.reference = reference;
        }

        public ReferenceInfo(Reference reference, boolean addToEnvironment, boolean addToPath)
        {
            this.reference = reference;
            this.addToEnvironment = addToEnvironment;
            this.addToPath = addToPath;
        }
    }
}
