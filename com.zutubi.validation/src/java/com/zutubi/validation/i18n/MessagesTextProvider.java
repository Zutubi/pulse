package com.zutubi.validation.i18n;

import com.zutubi.i18n.Messages;

/**
 * <class-comment/>
 */
public class MessagesTextProvider extends TextProviderSupport
{
    private Messages messages;

    public MessagesTextProvider(Object context)
    {
        messages = Messages.getInstance(context);
    }

    protected String lookupText(String key, Object... args)
    {
        return messages.format(key, args);
    }
}
