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

import com.zutubi.tove.config.ConfigurationValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.FieldValidatorSupport;

/**
 * Checks that essential (see {@link com.zutubi.tove.annotations.Essential})
 * fields are set up when the context indicates they should be checked (for
 * concrete instances when full validation takes place).
 */
public class EssentialValidator extends FieldValidatorSupport
{
    public EssentialValidator()
    {
        super("essential");
    }

    protected void validateField(Object value) throws ValidationException
    {
        if(validationContext instanceof ConfigurationValidationContext)
        {
            ConfigurationValidationContext configurationValidationContext = (ConfigurationValidationContext) validationContext;
            if(configurationValidationContext.isCheckEssential() && !configurationValidationContext.isTemplate() && value == null)
            {
                // Add as an instance rather than a field error as we are
                // applied to complex subfields.
                validationContext.addActionError(getErrorMessage());
            }
        }
    }
}
