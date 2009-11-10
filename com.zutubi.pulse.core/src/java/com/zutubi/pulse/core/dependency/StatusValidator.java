package com.zutubi.pulse.core.dependency;

import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.StringFieldValidatorSupport;
import com.zutubi.pulse.core.dependency.ivy.IvyStatus;

import java.util.List;

/**
 * A validator for a status field.
 */
public class StatusValidator extends StringFieldValidatorSupport
{
    protected void validateStringField(String value) throws ValidationException
    {
        List<String> validStatuses = IvyStatus.getStatuses();
        if (!validStatuses.contains(value))
        {
            addError("invalid", value);
        }
    }
}
