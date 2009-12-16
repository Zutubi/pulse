package com.zutubi.pulse.master.tove.config.user.contacts;

import com.zutubi.tove.config.ConfigurationValidationContext;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.FieldValidatorSupport;

import java.util.Map;

/**
 * A validator to ensure that a user has only one primary contact point.
 */
public class OnePrimaryContactValidator extends FieldValidatorSupport
{
    private ContactConfiguration thisContact;

    @Override
    public void validate(Object obj) throws ValidationException
    {
        thisContact = (ContactConfiguration) obj;
        validateField(getFieldValue(getFieldName(), obj));
    }

    @Override
    protected void validateField(Object value) throws ValidationException
    {
        boolean primary = (Boolean) value;
        if (primary)
        {
            ValidationContext context = getValidationContext();
            if (context instanceof ConfigurationValidationContext)
            {
                ConfigurationValidationContext configContext = (ConfigurationValidationContext) context;
                String parentPath = configContext.getParentPath();
                if (parentPath != null)
                {
                    checkForExistingPrimaryContact(parentPath, configContext);
                }
            }
        }
    }

    private void checkForExistingPrimaryContact(String parentPath, ConfigurationValidationContext configContext)
    {
        @SuppressWarnings({"unchecked"})
        Map<String, ContactConfiguration> contacts = (Map<String, ContactConfiguration>) configContext.getConfigurationTemplateManager().getInstance(parentPath);
        for (ContactConfiguration contact: contacts.values())
        {
            if (contact.getHandle() != thisContact.getHandle() && contact.isPrimary())
            {
                configContext.addActionError(getErrorMessage());
            }
        }
    }
}
