package com.zutubi.pulse.core;

import java.util.Collection;

/**
 * A hierarchy of reference containers.  If a reference is not found in a
 * scope, the request is deferred to the parent.
 */
public interface Scope extends ReferenceMap
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
     * Adds a reference which must have a unique name directly in this scope
     * (i.e. it may override an ancestor but may not replace a reference in
     * this scope itself).
     *
     * @param reference the reference to add
     * @throws IllegalArgumentException if a reference of the same name
     *         already exists directly in this scope
     */
    void addUnique(Reference reference) throws IllegalArgumentException;

    /**
     * Bulk version of {@link #addUnique}.
     *
     * @param references references to add
     * @throws IllegalArgumentException if any reference clashes names with
     *         an existing reference directly in this scope
     */
    void addAllUnique(Collection<? extends Reference> references) throws IllegalArgumentException;

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
}
