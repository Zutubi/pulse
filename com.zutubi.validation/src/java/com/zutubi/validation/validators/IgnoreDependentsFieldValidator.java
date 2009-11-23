package com.zutubi.validation.validators;

import com.zutubi.util.CollectionUtils;
import com.zutubi.validation.ValidationException;

/**
 * A validator that inspects the value of a field and decides based on this
 * if some dependent fields should be ignored for validation purposes.
 */
public class IgnoreDependentsFieldValidator extends FieldValidatorSupport
{
    private String[] nonIgnoreValues;
    private String[] dependentFields;

    public void setNonIgnoreValues(String... nonIgnoreValues)
    {
        this.nonIgnoreValues = nonIgnoreValues;
    }

    public void setDependentFields(String[] dependentFields)
    {
        this.dependentFields = dependentFields;
    }

    public void validateField(Object value) throws ValidationException
    {
        boolean found = false;
        if(value == null)
        {
            for(String nonIgnore: nonIgnoreValues)
            {
                if(nonIgnore == null)
                {
                    found = true;
                    break;
                }
            }
        }
        else
        {
            found = CollectionUtils.contains(nonIgnoreValues, value.toString());
        }

        if(!found)
        {
            if(dependentFields == null)
            {
                // Turn off field validation altogether
                getValidationContext().ignoreAllFields();
            }
            else
            {
                for(String dependentField: dependentFields)
                {
                    getValidationContext().addIgnoredField(dependentField);
                }
            }
        }
    }
}
