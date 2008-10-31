package com.zutubi.i18n.context;

import java.io.InputStream;

/**
 * A context loader is used to load a resource from a context.
 */
public interface ContextLoader
{
    /**
     * Load the named resource from the specified context.
     *
     * @param context the context from which we are loading the resource
     * @param resourceName the name of the resource we are loading
     *
     * @return the input stream attached to the resource.
     */
    InputStream loadResource(Context context, String resourceName);
}
