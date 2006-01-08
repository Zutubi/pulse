package com.cinnamonbob.core;

import com.cinnamonbob.core.validation.Validateable;
import com.opensymphony.xwork.validator.ValidatorContext;

/**
 */
public class SimpleValidateable implements Validateable
{
    public void validate(ValidatorContext context)
    {
        context.addFieldError("field", "error");
    }

}
