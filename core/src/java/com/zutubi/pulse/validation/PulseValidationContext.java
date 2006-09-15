package com.zutubi.pulse.validation;

import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.ValidationAware;
import com.zutubi.validation.i18n.LocaleProvider;
import com.zutubi.validation.i18n.TextProvider;
import com.zutubi.pulse.i18n.MessagesTextProvider;

/**
 * <class-comment/>
 */
public class PulseValidationContext extends DelegatingValidationContext
{
    public PulseValidationContext(Object obj)
    {
        super(obj);
    }

    public TextProvider makeTextPovider(Object obj, LocaleProvider localeProvider)
    {
        if (obj instanceof TextProvider)
        {
            return (TextProvider) obj;
        }
        return new MessagesTextProvider(obj);
    }
}
