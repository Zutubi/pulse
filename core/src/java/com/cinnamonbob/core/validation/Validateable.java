package com.cinnamonbob.core.validation;

import com.opensymphony.xwork.validator.ValidatorContext;

/**
 */
public interface Validateable
{
    void validate(ValidatorContext context);
}
