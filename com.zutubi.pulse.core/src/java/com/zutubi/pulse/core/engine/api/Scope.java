package com.zutubi.pulse.core.engine.api;

import com.zutubi.tove.variables.api.Variable;
import com.zutubi.tove.variables.api.VariableMap;

import java.util.Collection;

/**
 * A hierarchy of variable containers.  If a variable is not found in a
 * scope, the request is deferred to the parent.
 */
public interface Scope extends VariableMap
{
    /**
     * @see #getAncestor(String)
     *
     * @return the label for this scope, may be null
     */
    String getLabel();

    /**
     * Marks this scope with the given label.
     *
     * @see #getAncestor(String)
     *
     * @param label the new label
     */
    void setLabel(String label);

    /**
     * Creates a new Scope with this same type as this and with this as its
     * parent.
     *
     * @return the new child scope
     */
    Scope createChild();

    /**
     * @return the parent of this scope, or null if this is the root of the
     *         chain
     */
    Scope getParent();

    /**
     * @return the root of the scope chain (the scope with no parent)
     */
    Scope getRoot();

    /**
     * Searches the scope chain from this to root for the scope with the
     * given label.
     *
     * @param label the label to search for
     * @return the scope with the given label, or null if no such scope
     *         exists
     */
    Scope getAncestor(String label);

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

    /**
     * Adds a variable which must have a unique name directly in this scope
     * (i.e. it may override an ancestor but may not replace a variable in
     * this scope itself).
     *
     * @param variable the variable to add
     * @throws IllegalArgumentException if a variable of the same name
     *         already exists directly in this scope
     */
    void addUnique(Variable variable) throws IllegalArgumentException;

    /**
     * Bulk version of {@link #addUnique}.
     *
     * @param variables variables to add
     * @throws IllegalArgumentException if any variable clashes names with
     *         an existing variable directly in this scope
     */
    void addAllUnique(Collection<? extends Variable> variables) throws IllegalArgumentException;

    /**
     * Copies a chain of scopes up to but not including the passed in scope.
     * If the passed in scope is not in the chain or is null, all scopes are
     * copied.  Scopes are compared for object identity.
     *
     * @param scope the scope to copy up to (not included in the copy),
     *        should be a proper ancestor of this scope or null
     * @return a copy of the scope chain from this scope up to just before
     *         the given scope
     */
    Scope copyTo(Scope scope);

    /**
     * @return a copy of this entire scope chain
     */
    Scope copy();

    void add(Collection<? extends ResourceProperty> properties);

    void add(ResourceProperty resourceProperty);
}
