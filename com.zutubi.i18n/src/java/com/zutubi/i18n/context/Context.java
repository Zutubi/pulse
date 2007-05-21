package com.zutubi.i18n.context;

import java.io.InputStream;

/**
 * <class-comment/>
 */
public interface Context
{
    InputStream getResourceAsStream(String name);
}
