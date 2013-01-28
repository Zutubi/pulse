package com.zutubi.pulse.core;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.engine.api.Scope;
import com.zutubi.tove.variables.GenericVariable;
import com.zutubi.tove.variables.VariableResolver;
import com.zutubi.tove.variables.api.ResolutionException;
import com.zutubi.tove.variables.api.Variable;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.StringUtils;

import java.io.File;
import java.util.*;

/**
 * A scope holds named variables and has a parent.  When looking up a
 * variable by name, if it is not found in this scope the lookup is
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
    private Map<String, VariableInfo> variables = new LinkedHashMap<String, VariableInfo>();

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

    private Map<String, VariableInfo> merge()
    {
        Map<String, VariableInfo> merged = new LinkedHashMap<String, VariableInfo>();
        if(parent != null)
        {
            merged.putAll(parent.merge());
        }

        // We do not use putAll here as the LinkedHashMap does not preserve
        // ordering for re-entries (i.e. putting on top of an existing key)
        // whereas we want to preserve ordering.  Hence re-entries are
        // avoided by removing existing keys.
        for(Map.Entry<String, VariableInfo> entry: variables.entrySet())
        {
            if(merged.containsKey(entry.getKey()))
            {
                merged.remove(entry.getKey());
            }

            merged.put(entry.getKey(), entry.getValue());
        }

        return merged;
    }
    
    public Collection<Variable> getVariables()
    {
        return CollectionUtils.map(merge().values(), new Mapping<VariableInfo, Variable>()
        {
            public Variable map(VariableInfo variableInfo)
            {
                return variableInfo.variable;
            }
        });
    }

    private Collection<Variable> getVariablesSatisfyingPredicate(Predicate<VariableInfo> p)
    {
        return CollectionUtils.map(Collections2.filter(merge().values(), p), new Mapping<VariableInfo, Variable>()
        {
            public Variable map(VariableInfo variableInfo)
            {
                return variableInfo.variable;
            }
        });
    }

    public Collection<Variable> getVariables(final Class type)
    {
        return getVariablesSatisfyingPredicate(new Predicate<VariableInfo>()
        {
            public boolean apply(VariableInfo variableInfo)
            {
                return type.isInstance(variableInfo.variable.getValue());
            }
        });
    }

    public boolean containsVariable(String name)
    {
        return getVariable(name) != null;
    }

    /**
     * Locates the closest scope in which a variable of the given name is
     * defined.
     * 
     * @param name name of the variable to find
     * @return the closest scope in which the variable is defined, or null if
     *         it is not defined
     */
    public PulseScope findVariable(String name)
    {
        if (variables.containsKey(name))
        {
            return this;
        }
        else if (parent == null)
        {
            return null;
        }
        else
        {
            return parent.findVariable(name);
        }
    }

    public Variable getVariable(String name)
    {
        VariableInfo info = merge().get(name);
        Variable result = info == null ? null : info.variable;

        if(name.startsWith("env."))
        {
            // Lets see if there is a property value that overrides the default environment variable.
            // This override is the behaviour we see when actually running a Pulse command.
            String envName = name.substring(4);
            Map<String, String> environment = getEnvironment();
            String value = environment.get(envName);
            if(value != null)
            {
                result = new GenericVariable<String>(name, value);
            }

            // Special case PATH to add the prefix
            if(envName.toUpperCase(Locale.US).equals(ENV_PATH))
            {
                String pathPrefix = getPathPrefix();
                if(StringUtils.stringSet(pathPrefix))
                {
                    if(result == null)
                    {
                        result = new GenericVariable<String>(name, pathPrefix.substring(0, pathPrefix.length() - 1));
                    }
                    else if((result.getValue() instanceof String))
                    {
                        result = new GenericVariable<String>(name, pathPrefix + result.getValue());
                    }
                }
            }
        }

        return result;
    }

    public <T> T getVariableValue(String name, Class<T> type)
    {
        Variable r = getVariable(name);
        if (r == null || !type.isInstance(r.getValue()))
        {
            return null;
        }

        return type.cast(r.getValue());
    }

    public void addUnique(Variable variable) throws IllegalArgumentException
    {
        if (variables.containsKey(variable.getName()))
        {
            throw new IllegalArgumentException("'" + variable.getName() + "' is already defined in this scope.");
        }

        add(variable);
    }

    public void add(PulseScope other)
    {
        for (VariableInfo info: other.merge().values())
        {
            variables.put(info.variable.getName(), info);
        }
    }

    public void add(Variable variable)
    {
        variables.put(variable.getName(), new VariableInfo(variable));
    }

    public void addAll(Collection<? extends Variable> variables)
    {
        for (Variable r : variables)
        {
            add(r);
        }
    }

    public void addAllUnique(Collection<? extends Variable> variables) throws IllegalArgumentException
    {
        for (Variable r : variables)
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
        Collection<Variable> variables = getVariablesSatisfyingPredicate(new Predicate<VariableInfo>()
        {
            public boolean apply(VariableInfo variableInfo)
            {
                return variableInfo.addToEnvironment && (variableInfo.variable.getValue() instanceof String);
            }
        });

        Map<String, String> result = new HashMap<String, String>(variables.size());
        for(Variable r: variables)
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
        if(StringUtils.stringSet(pathPrefix))
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
        Collection<Variable> variables = getVariablesSatisfyingPredicate(new Predicate<VariableInfo>()
        {
            public boolean apply(VariableInfo variableInfo)
            {
                return variableInfo.addToPath && variableInfo.variable.getValue() instanceof String;
            }
        });
        
        List<String> result = CollectionUtils.map(variables, new Mapping<Variable, String>()
        {
            public String map(Variable variable)
            {
                return (String) variable.getValue();
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
        try
        {
            value = VariableResolver.resolveVariables(value, this, VariableResolver.ResolutionStrategy.RESOLVE_NON_STRICT);
        }
        catch (ResolutionException e)
        {
            // Just use unresolved value.
        }

        String name = resourceProperty.getName();
        variables.put(name, new VariableInfo(new GenericVariable<String>(name, value), resourceProperty.getAddToEnvironment(), resourceProperty.getAddToPath()));
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

        add(new GenericVariable<String>("env." + name, value));
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
        copy.variables = new LinkedHashMap<String, VariableInfo>(variables);
        return copy;
    }

    public PulseScope copy()
    {
        return copyTo(null);
    }

    private static class VariableInfo
    {
        private Variable variable;
        private boolean addToEnvironment = false;
        private boolean addToPath = false;

        public VariableInfo(Variable variable)
        {
            this.variable = variable;
        }

        public VariableInfo(Variable variable, boolean addToEnvironment, boolean addToPath)
        {
            this.variable = variable;
            this.addToEnvironment = addToEnvironment;
            this.addToPath = addToPath;
        }
    }
}
