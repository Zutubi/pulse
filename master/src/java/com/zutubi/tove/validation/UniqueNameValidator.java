package com.zutubi.tove.validation;

import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.ConfigurationValidationContext;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.StringFieldValidatorSupport;

/**
 * A validator that ensures unique names are used for instances added to
 * maps.  This ensures that user-selected names within a configuration scope
 * do not conflict.
 */
public class UniqueNameValidator extends StringFieldValidatorSupport
{
    public UniqueNameValidator()
    {
        super("inuse");
    }

    public void validateStringField(String name) throws ValidationException
    {
        ValidationContext context = getValidationContext();
        if(context instanceof ConfigurationValidationContext)
        {
            ConfigurationValidationContext configContext = (ConfigurationValidationContext) context;
            String parentPath = configContext.getParentPath();

            // We can only validate string keys of map instances.
            if(parentPath != null)
            {
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
