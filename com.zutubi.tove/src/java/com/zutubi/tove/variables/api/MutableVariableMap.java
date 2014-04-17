package com.zutubi.tove.variables.api;

import java.util.Collection;

/**
 * An extension of {@link com.zutubi.tove.variables.api.VariableMap} which allows variables to be
 * added.
 */
public interface MutableVariableMap extends VariableMap
{
    void add(Variable variable);

    void addAll(Collection<? extends Variable> variables);
}
