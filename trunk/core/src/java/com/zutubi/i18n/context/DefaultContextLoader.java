package com.zutubi.i18n.context;

import java.io.InputStream;

/**
 * <class-comment/>
 */
public class DefaultContextLoader implements ContextLoader
{
    public InputStream loadResource(Context context, String resourceName)
    {
        // load from the current class loader.
        return getClass().getResourceAsStream("/" + resourceName);
    }
}
