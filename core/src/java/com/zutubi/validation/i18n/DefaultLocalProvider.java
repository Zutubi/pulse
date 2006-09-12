package com.zutubi.validation.i18n;

import java.util.Locale;

/**
 * <class-comment/>
 */
public class DefaultLocalProvider implements LocaleProvider
{
    public Locale getLocale()
    {
        return Locale.getDefault();
    }
}
