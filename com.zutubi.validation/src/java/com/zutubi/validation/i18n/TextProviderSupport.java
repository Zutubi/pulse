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

    public String getText(String key, Object... args)
    {
        if (args == null)
        {
            args = new Object[0];
        }
        return lookupText(key, args);
    }

    public TextProvider getTextProvider(Object context)
    {
        return this;
    }

    protected abstract String lookupText(String key, Object... args);
}
