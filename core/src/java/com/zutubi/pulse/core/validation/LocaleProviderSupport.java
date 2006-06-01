package com.zutubi.pulse.core.validation;

import com.opensymphony.xwork.LocaleProvider;

import java.util.Locale;

/**
 *
 *
 */
public class LocaleProviderSupport implements LocaleProvider
{
    public Locale getLocale()
    {
        return Locale.getDefault();
    }
}
