package com.zutubi.i18n.bundle;

import java.io.InputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.List;

/**
 * <class-comment/>
 */
public class BaseResourceBundleFactory implements ResourceBundleFactory
{
    private static final Expander expander = new Expander();

    public BaseBundle loadBundle(InputStream input, Locale locale) throws IOException
    {
        return new BaseResourceBundle(input, locale);
    }

    public List<String> expand(String baseName, Locale locale)
    {
        return expander.expand(baseName, locale, ".properties");
    }
}
