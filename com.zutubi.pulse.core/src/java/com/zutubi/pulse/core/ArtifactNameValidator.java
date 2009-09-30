package com.zutubi.pulse.core;

import com.zutubi.validation.validators.RegexValidator;

/**
 */
public class ArtifactNameValidator extends RegexValidator
{
    public ArtifactNameValidator()
    {
        setPattern("[a-zA-Z0-9][-a-zA-Z0-9_. ]*");
    }
}
