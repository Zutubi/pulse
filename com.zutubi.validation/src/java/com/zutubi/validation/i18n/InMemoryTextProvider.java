package com.zutubi.validation.i18n;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public class InMemoryTextProvider extends TextProviderSupport
{
    private Map<String, String> texts = new HashMap<String, String>();

    public void addText(String key, String text)
    {
        texts.put(key, text);
    }

    protected String lookupText(String key, Object... args)
    {
        String value = texts.get(key);
        if (value != null)
        {
            value = MessageFormat.format(value, args);
        }
        return value;
    }
}

