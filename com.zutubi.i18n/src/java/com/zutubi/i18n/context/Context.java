package com.zutubi.i18n.context;

import java.io.InputStream;

/**
 * A Context provides two functions.  It holds the information necessary
 * for a ContextResolver to resolve the paths that will be used to lookup
 * the resource bundles associated with the context. Secondly, it defines
 * how the resolved resources are then loaded.
 */
public interface Context
{
    /**
     * Retrieve the named resource from within this context.  This method
     * is used by the default context loader implementation to load a
     * resource.
     *
     * @param name the name of the resource
     *
     * @return the input stream attached to the named resource, or
     * null if the resource could not be located.
     */
    InputStream getResourceAsStream(String name);
}
