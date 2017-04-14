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
