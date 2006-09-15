package com.zutubi.validation.i18n;

/**
 * <class-comment/>
 */
public abstract class TextProviderSupport implements TextProvider
{
    public String getText(String key)
    {
        return getText(key, new Object[0]);
    }

    public String getText(String key, String defaultValue)
    {
        return getText(key, defaultValue, new Object[0]);
    }

    public String getText(String key, Object... args)
    {
        return getText(key, null, args);
    }

    public String getText(String key, String defaultValue, Object... args)
    {
        if (args == null)
        {
            args = new Object[0];
        }
        String text = lookupText(key, args);
        if (text != null)
        {
            return text;
        }
        return defaultValue;
    }

    protected abstract String lookupText(String key, Object... args);
}
