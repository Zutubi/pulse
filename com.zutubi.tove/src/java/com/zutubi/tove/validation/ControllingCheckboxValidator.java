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

import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.FieldValidatorSupport;
import com.zutubi.validation.validators.IgnoreDependentsFieldValidator;

/**
 * A validator that ignores dependent fields when a controlling checkbox is
 * in a certain state.  Delegates to a generic field ignoring validator.
 */
public class ControllingCheckboxValidator extends FieldValidatorSupport
{
    private String[] checkedFields;
    private String[] uncheckedFields;

    public void setCheckedFields(String[] checkedFields)
    {
        this.checkedFields = checkedFields;
    }

    public void setUncheckedFields(String[] uncheckedFields)
    {
        this.uncheckedFields = uncheckedFields;
    }

    public void validateField(Object object) throws ValidationException
    {
        if (checkedFields.length == 0 && uncheckedFields.length == 0)
        {
            IgnoreDependentsFieldValidator delegate = createDelegate();
            delegate.setNonIgnoreValues(Boolean.toString(true));
            delegate.validateField(object);
        }
        else
        {
            if (checkedFields.length > 0)
            {
                IgnoreDependentsFieldValidator checkedDelegate = createDelegate();
                checkedDelegate.setNonIgnoreValues(Boolean.toString(true));
                checkedDelegate.setDependentFields(checkedFields);
                checkedDelegate.validateField(object);
            }

            if (uncheckedFields.length > 0)
            {
                IgnoreDependentsFieldValidator uncheckedDelegate = createDelegate();
                uncheckedDelegate.setNonIgnoreValues(Boolean.toString(false));
                uncheckedDelegate.setDependentFields(uncheckedFields);
                uncheckedDelegate.validateField(object);
            }
        }
    }

    private IgnoreDependentsFieldValidator createDelegate()
    {
        IgnoreDependentsFieldValidator delegate = new IgnoreDependentsFieldValidator();
        delegate.setFieldName(getFieldName());
        delegate.setValidationContext(validationContext);
        return delegate;
    }
}
