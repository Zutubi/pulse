package com.zutubi.pulse.core.dependency;

import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.StringFieldValidatorSupport;

import java.util.List;

/**
 * A validator for a status field.
 */
public class StatusValidator extends StringFieldValidatorSupport
{
    private DependencyManager dependencyManager;

    protected void validateStringField(String value) throws ValidationException
    {
        List<String> validStatuses = dependencyManager.getStatuses();
        if (!validStatuses.contains(value))
        {
            addError("invalid", value);
        }
    }

    public void setDependencyManager(DependencyManager dependencyManager)
    {
        this.dependencyManager = dependencyManager;
    }
}
