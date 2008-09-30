package com.zutubi.pulse.validation;

import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.ValidationAware;
import com.zutubi.validation.ValidationAwareSupport;
import com.zutubi.validation.i18n.TextProvider;
import com.zutubi.validation.i18n.MessagesTextProvider;

/**
 * <class-comment/>
 */
public class PulseValidationContext extends DelegatingValidationContext
{
    public PulseValidationContext(ValidationAware validationAware, TextProvider textProvider)
    {
        super(validationAware, textProvider);
    }

    public PulseValidationContext(Object obj)
    {
        super(obj);
    }


    public PulseValidationContext(TextProvider textProvider)
    {
        super(new ValidationAwareSupport(), textProvider);
    }

    public TextProvider getTextProvider(Object context)
    {
        return new MessagesTextProvider(context);
    }
}
