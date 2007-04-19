package com.zutubi.i18n.context;

import java.util.Locale;

/**
 * <class-comment/>
 */
public class ContextKey
{
    public String key;

    public static long generate(Context context, Locale locale)
    {
        return (context.toString() + "-" + locale.toString()).hashCode();
    }
}
