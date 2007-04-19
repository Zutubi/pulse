package com.zutubi.i18n.format;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.text.MessageFormat;

/**
 * The default formatter implementation.  This implementation delegates the
 * formatting to the MessageFormat object.
 * 
 */
public class Formatter
{
    public Formatter()
    {
    }

    public String format(ResourceBundle bundle, String key)
    {
        return formatArgs(bundle, key);
    }

    public String format(ResourceBundle bundle, String key, Object... args)
    {
        return formatArgs(bundle, key, args);
    }

    private String formatArgs(ResourceBundle bundle, String key, Object... args)
    {
        if (null != bundle)
        {
            try
            {
                return formatArgs(bundle.getString(key), args);
            }
            catch (MissingResourceException e)
            {
                // this key is not located in the specified bundle.
            }
        }
        return null;
    }

    private String formatArgs(String text, Object... args)
    {
        return MessageFormat.format(text, args);
    }

}