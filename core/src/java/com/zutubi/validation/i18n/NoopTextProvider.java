package com.zutubi.validation.i18n;

/**
 * <class-comment/>
 */
public class NoopTextProvider implements TextProvider
{
    public String getText(String key)
    {
        return key;
    }
}
