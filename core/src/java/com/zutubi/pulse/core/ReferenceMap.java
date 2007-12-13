package com.zutubi.pulse.core;

import java.util.Collection;

/**
 * A container for references which can be looked up by name.
 */
public interface ReferenceMap
{
    boolean containsReference(String name);

    Reference getReference(String name);

    Collection<Reference> getOldrefs();

    void add(Reference reference);

    void addAll(Collection<? extends Reference> references);
}
