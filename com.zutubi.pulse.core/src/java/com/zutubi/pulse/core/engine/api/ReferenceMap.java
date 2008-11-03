package com.zutubi.pulse.core.engine.api;

import java.util.Collection;

/**
 * A container for references which can be looked up by name.
 */
public interface ReferenceMap
{
    /**
     * Returns true if this reference map contains a reference of the specified name.
     *
     * @param name the name of the reference.
     *
     * @return true if this instance contains the reference, false otherwise.
     */
    boolean containsReference(String name);

    /**
     * Get the named reference.
     *
     * @param name the name of the reference
     *
     * @return the reference, or null if this reference map does not contain the reference.
     */
    Reference getReference(String name);

    /**
     * Return all of the references contained by this map as a collection.
     *
     * @return a collection of all the references.
     */
    Collection<Reference> getReferences();

    /**
     * Add a reference to this reference map. The key by which this reference will
     * later be accessible is the name value of the reference.
     *
     * If this map already contains a reference of the same name as the parameter, the
     * old reference instance will be replaced.
     *
     * @param reference the reference instance to be added
     */
    void add(Reference reference);

    /**
     * Add all of the references contained by the references collection to this
     * reference map.
     *
     * @param references the collection of references to be added.
     */
    void addAll(Collection<? extends Reference> references);
}
