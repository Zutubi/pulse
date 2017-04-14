/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.tove.config.user.contacts;

import com.zutubi.tove.config.ConfigurationValidationContext;
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
            ConfigurationValidationContext configContext = (ConfigurationValidationContext) getValidationContext();
            String parentPath = configContext.getParentPath();
            if (parentPath != null)
            {
                checkForExistingPrimaryContact(parentPath, configContext);
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
