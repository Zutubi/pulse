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

}
