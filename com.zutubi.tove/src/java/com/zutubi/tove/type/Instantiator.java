package com.zutubi.tove.type;

import com.zutubi.tove.config.Configuration;

/**
 * A callback interface used by types to instantiate their properties (or
 * child items in the case of a collection).  This allows extra logic to be
 * performed on the instances, such as caching, without the knowledge of the
 * types.
 */
public interface Instantiator
{
    /**
     * Instantiates the given property using the type and data provided.
     * Errors are recorded on the instantiated instance, except where the
     * error prevents the instance creation.
     *
     * @param path     path of the value being instantiated
     * @param relative if true, the path is relative to the current instance,
     *                 otherwise the path is absolute
     * @param type     actual type of the property
     * @param data     the property data (e.g. record, string)
     * @return the instance
     *
     * @throws TypeException if an error prevents instance creation
     */
    Object instantiate(String path, boolean relative, Type type, Object data) throws TypeException;

    /**
     * Resolves a reference given the referred-to handle.  The referred-to
     * object may be instantiated on demand in some circumstances.
     *
     * @param toHandle the handle of the referenced object
     * @return the reference object
     * @throws TypeException if the reference cannot be resolved
     */
    Configuration resolveReference(long toHandle) throws TypeException;
}
