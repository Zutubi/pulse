package com.zutubi.tove.config;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.Instantiator;
import com.zutubi.tove.type.TypeException;

/**
 * A basic interface for reference resolution.
 *
 * @see com.zutubi.tove.config.ConfigurationReferenceManager
 */
public interface ReferenceResolver
{
    /**
     * Resolves a reference from within the given template owner to the given
     * handle.  This may prompt just-in-time instantiation of the referenced
     * instance.
     *
     * @param templateOwnerPath if in a templated scope, the item of the
     *                          templated collection that this reference is
     *                          coming from, otherwise null
     * @param toHandle          the handle being referenced - not necessarily
     *                          the handle of the referenced record in a
     *                          templated scope
     * @param instantiator      used for just-in-time instantiation if the
     *                          referenced object has not yet been built
     * @param indexPath         if not null, the from path used to add an
     *                          entry to the index of references (a mapping
     *                          from a path to all from paths that reference
     *                          it).
     * @return the instance that the reference resolves to
     * @throws TypeException on any error in just in time instantiation
     */
    Configuration resolveReference(String templateOwnerPath, long toHandle, Instantiator instantiator, String indexPath) throws TypeException;
}
