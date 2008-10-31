package com.zutubi.i18n.context;

import java.io.InputStream;

/**
 * The default implementation of the context loader simply delegates the
 * loading to the contexts getResourceAsStream method.
 */
public class DefaultContextLoader implements ContextLoader
{
    public InputStream loadResource(Context context, String resourceName)
    {
        // load from the current class loader.
        return context.getResourceAsStream("/" + resourceName);
    }
}
