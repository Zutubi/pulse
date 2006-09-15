package com.zutubi.validation.i18n;

/**
 * <class-comment/>
 */
public class ChainingTextProvider implements TextProvider
{
    private TextProvider[] providers = null;

    public ChainingTextProvider(TextProvider... providers)
    {
        this.providers = providers;
    }

    public String getText(final String key)
    {
        for (TextProvider provider : providers)
        {
            String text = provider.getText(key);
            if (text != null)
            {
                return text;
            }
        }
        return null;
    }

    public String getText(String key, String defaultValue)
    {
        for (TextProvider provider : providers)
        {
            String text = provider.getText(key, defaultValue);
            if (text != null)
            {
                return text;
            }
        }
        return null;
    }

    public String getText(String key, Object... args)
    {
        for (TextProvider provider : providers)
        {
            String text = provider.getText(key, args);
            if (text != null)
            {
                return text;
            }
        }
        return null;
    }

    public String getText(String key, String defaultValue, Object... args)
    {
        for (TextProvider provider : providers)
        {
            String text = provider.getText(key, defaultValue, args);
            if (text != null)
            {
                return text;
            }
        }
        return null;
    }
}
