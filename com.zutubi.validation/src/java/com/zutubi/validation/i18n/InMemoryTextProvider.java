package com.zutubi.validation.i18n;

import java.util.Map;
import java.util.HashMap;

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
        return texts.get(key);
    }
}

