package com.zutubi.pulse.validation;

import com.zutubi.validation.DefaultValidationManager;
import com.zutubi.validation.providers.AnnotationValidatorProvider;
import com.zutubi.validation.providers.ReflectionValidatorProvider;

/**
 * <class-comment/>
 */
public class PulseValidationManager extends DefaultValidationManager
{
    public PulseValidationManager()
    {
        addValidatorProvider(new AnnotationValidatorProvider());
        addValidatorProvider(new ReflectionValidatorProvider());
    }
}
