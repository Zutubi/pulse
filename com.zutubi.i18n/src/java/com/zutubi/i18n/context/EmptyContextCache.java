package com.zutubi.i18n.context;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A cache that never stores anything.  Useful for development where caching
 * is not desirable.
 */
public class EmptyContextCache implements ContextCache
{
    public void addToCache(Context context, Locale locale, List<ResourceBundle> bundle)
    {
    }

    public List<ResourceBundle> getFromCache(Context context, Locale locale)
    {
        return Collections.emptyList();
    }

    public boolean isCached(Context context, Locale locale)
    {
        return false;
    }

    public void clear()
    {
    }
}
