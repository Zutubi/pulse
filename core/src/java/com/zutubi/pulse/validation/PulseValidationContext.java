package com.zutubi.pulse.validation;

import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.i18n.TextProvider;

/**
 * <class-comment/>
 */
public class PulseValidationContext extends DelegatingValidationContext
{
    public PulseValidationContext(Object obj)
    {
        super(obj);
    }

    public TextProvider getTextProvider(Object context)
    {
        return new MessagesTextProvider(context);
    }
}
