package com.zutubi.i18n.context;

/**
 * <class-comment/>
 */
public interface ContextResolver<T extends Context>
{
    String[] resolve(T context);

    Class<T> getContextType();
}
