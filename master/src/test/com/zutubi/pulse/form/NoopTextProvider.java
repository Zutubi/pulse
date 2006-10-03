package com.zutubi.pulse.form;

/**
 * 
 */
public class NoopTextProvider implements TextProvider
{
    public String getText(String key)
    {
        return key;
    }
}
