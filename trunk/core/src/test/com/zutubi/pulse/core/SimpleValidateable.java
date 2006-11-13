package com.zutubi.pulse.core;

import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

/**
 */
public class SimpleValidateable implements Validateable
{
    public void validate(ValidationContext context)
    {
        context.addFieldError("field", "error");
    }

}
