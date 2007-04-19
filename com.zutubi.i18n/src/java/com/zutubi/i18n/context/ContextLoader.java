package com.zutubi.i18n.context;

import java.io.InputStream;

/**
 * <class-comment/>
 */
public interface ContextLoader
{
    InputStream loadResource(Context context, String resourceName);
}
