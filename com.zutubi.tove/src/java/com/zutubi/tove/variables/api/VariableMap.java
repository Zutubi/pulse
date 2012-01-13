package com.zutubi.tove.variables.api;

import java.util.Collection;

/**
 * A container for variables which can be looked up by name.
 */
public interface VariableMap
{
    /**
     * Returns true if this map contains a variable of the specified name.
     *
     * @param name the name of the variable.
     * @return true if this instance contains the variable, false otherwise.
     */
    boolean containsVariable(String name);

    /**
     * Get the named variable.
     *
     * @param name the name of the variable
     * @return the variable, or null if this map does not contain the variable.
     */
    Variable getVariable(String name);

    /**
     * Return all of the variables contained by this map as a collection.
     *
     * @return a collection of all the variable.
     */
    Collection<Variable> getVariables();

    /**
     * Add a variable to this map. The key by which this variable will later be
     * accessible is the name value of the variable.
     *
     * If this map already contains a variable of the same name as the
     * parameter, the old variable instance will be replaced.
     *
     * @param variable the variable instance to be added
     */
    void add(Variable variable);

    /**
     * Add all of the variables contained by the given collection to this map.
     *
     * @param variables the collection of variables to be added.
     */
    void addAll(Collection<? extends Variable> variables);
}
