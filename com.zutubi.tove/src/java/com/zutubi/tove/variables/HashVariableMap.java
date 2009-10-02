package com.zutubi.tove.variables;

import com.zutubi.tove.variables.api.Variable;
import com.zutubi.tove.variables.api.VariableMap;

import java.util.Collection;
import java.util.HashMap;

/**
 * An implementation of the {@link com.zutubi.tove.variables.api.VariableMap}
 * interface backed by a {@link java.util.HashMap}.
 */
public class HashVariableMap implements VariableMap
{
    private HashMap<String, Variable> variables = new HashMap<String, Variable>();

    public boolean containsVariable(String name)
    {
        return variables.containsKey(name);
    }

    public Variable getVariable(String name)
    {
        return variables.get(name);
    }

    public Collection<Variable> getVariables()
    {
        return variables.values();
    }

    public void add(Variable variable)
    {
        variables.put(variable.getName(), variable);
    }

    public void addAll(Collection<? extends Variable> variables)
    {
        for (Variable r : variables)
        {
            add(r);
        }
    }
}
