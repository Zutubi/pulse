package com.zutubi.pulse.core.validation;

import com.zutubi.validation.DefaultValidationManager;
import com.zutubi.validation.providers.AnnotationValidatorProvider;
import com.zutubi.validation.providers.ReflectionValidatorProvider;

/**
 * An extension of the default validation manager configured with the
 * pre-configured as required for Pulse.
 */
public class PulseValidationManager extends DefaultValidationManager
{
    public PulseValidationManager()
    {
        addValidatorProvider(new AnnotationValidatorProvider());
        addValidatorProvider(new ReflectionValidatorProvider());
    }
}
