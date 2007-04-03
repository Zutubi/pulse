package com.zutubi.pulse.validation;

import com.zutubi.validation.i18n.TextProviderSupport;
import com.zutubi.pulse.i18n.Messages;

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

    public MessagesTextProvider(Class context)
    {
        this.context = context;
    }

    protected String lookupText(String key, Object... args)
    {
        return Messages.format(context, key, args);
    }
}
