package com.zutubi.tove.variables;

import com.zutubi.tove.variables.api.MutableVariableMap;
import com.zutubi.tove.variables.api.Variable;

import java.util.Collection;
import java.util.HashMap;

/**
 * An implementation of the {@link com.zutubi.tove.variables.api.VariableMap}
 * interface backed by a {@link java.util.HashMap}.
 */
public class HashVariableMap implements MutableVariableMap
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        HashVariableMap that = (HashVariableMap) o;
        return variables.equals(that.variables);
    }

    @Override
    public int hashCode()
    {
        return variables.hashCode();
    }

    @Override
    public String toString()
    {
        return variables.values().toString();
    }
}
