package com.zutubi.pulse.i18n;

/**
 * <class-comment/>
 */
public class MessagesTextProvider implements TextProvider
{
    private Object context;

    public MessagesTextProvider(Object context)
    {
        this.context = context;
    }

    public String getText(String key)
    {
        return Messages.format(context, key);
    }
}
