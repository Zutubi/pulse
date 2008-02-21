package com.zutubi.prototype.validation;

import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.config.ConfigurationValidationContext;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.FieldValidatorSupport;

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
            String parentPath = configContext.getParentPath();

            // We can only validate string keys of map instances.
            if(value != null && value instanceof String && parentPath != null)
            {
                String name = (String) value;
                String baseName = configContext.getBaseName();

                // If this is a new object, or the object's name has changed,
                // check that the name is unique in the parent.
                if(baseName == null || !baseName.equals(name))
                {
                    ConfigurationTemplateManager configurationTemplateManager = configContext.getConfigurationTemplateManager();
                    try
                    {
                        configurationTemplateManager.validateNameIsUnique(parentPath, name, getFieldName(), validationContext);
                    }
                    catch(ValidationException e)
                    {
                        validationContext.addFieldError(getFieldName(), e.getMessage());
                    }
                }
            }
        }
    }
}
