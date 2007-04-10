package com.zutubi.prototype.validation;

import com.zutubi.validation.validators.FieldValidatorSupport;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.ValidationContext;
import com.zutubi.prototype.config.ConfigurationValidationContext;

import java.util.Map;

/**
 * A validator that ensures unique names are used for instances added to
 * maps.  This ensures that user-selected names within a configuration scope
 * do not conflict.
 */
public class UniqueNameValidator extends FieldValidatorSupport
{
    public UniqueNameValidator()
    {
        setDefaultMessageKey(".inuse");
    }

    public void validate(Object obj) throws ValidationException
    {
        ValidationContext context = getValidationContext();
        if(context instanceof ConfigurationValidationContext)
        {
            ConfigurationValidationContext configContext = (ConfigurationValidationContext) context;
            Object value = getFieldValue(getFieldName(), obj);
            Object parent = configContext.getParentInstance();

            // We can only validate string keys of map instances.
            if(value != null && value instanceof String && parent != null && parent instanceof Map)
            {
                String name = (String) value;
                String baseName = configContext.getBaseName();

                // If this is a new object, or the object's name has changed,
                // check that the name is unique in the parent.
                if(baseName == null || !baseName.equals(name))
                {
                    Map parentMap = (Map) parent;
                    if(parentMap.containsKey(name))
                    {
                        addFieldError(getFieldName());
                    }
                }
            }
        }
    }
}
