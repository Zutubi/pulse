package com.zutubi.i18n.bundle;

import java.util.*;
import java.io.InputStream;
import java.io.IOException;

/**
 * <class-comment/>
 */
public class BaseResourceBundle extends BaseBundle
{
    private Map<String, String> messages;
    private Locale currentLocale;

    public Locale getLocale()
    {
        return currentLocale;
    }

    public BaseResourceBundle(Locale locale)
    {
        this.currentLocale = locale;
    }

    public BaseResourceBundle(InputStream stream, Locale locale) throws IOException
    {
        Properties properties = new Properties();
        properties.load(stream);
        messages = new HashMap(properties);
        this.currentLocale = locale;
    }

    protected Object handleGetObject(String key)
    {
        if (null == key)
        {
            throw new NullPointerException();
        }
        return messages.get(key);
    }

    public Enumeration<String> getKeys()
    {
        ResourceBundle parent = this.parent;
        // need to add parent keys
        return Collections.enumeration(messages.keySet());
    }
}