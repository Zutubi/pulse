package com.zutubi.pulse.core;

import java.util.Collection;

/**
 * A hierarchy of reference containers.  If a reference is not found in a
 * scope, the request is deferred to the parent.
 */
public interface Scope extends ReferenceMap
{
    Scope createChild();

    Scope getParent();

    Scope getRoot();

    void addUnique(Reference reference) throws IllegalArgumentException;

    void addAllUnique(Collection<? extends Reference> references) throws IllegalArgumentException;

    Scope copyTo(Scope scope);

    Scope copy();
}
