package com.zutubi.pulse.i18n;

import com.zutubi.validation.i18n.TextProviderSupport;

/**
 * <class-comment/>
 */
public class MessagesTextProvider extends TextProviderSupport
{
    private Object context;

    public MessagesTextProvider(Object context)
    {
        this.context = context;
    }

    protected String lookupText(String key, Object... args)
    {
        return Messages.format(context, key, args);
    }
}
