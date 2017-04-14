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
 * A validator that ignores dependent fields when a controlling select is
 * in certain states.  Delegates to a generic field ignoring validator.
 */
public class ControllingSelectValidator extends FieldValidatorSupport
{
    private String[] enableSet;
    private String[] dependentFields;

    public void setEnableSet(String[] enableSet)
    {
        this.enableSet = enableSet;
    }

    public void setDependentFields(String[] dependentFields)
    {
        this.dependentFields = dependentFields;
    }

    public void validateField(Object object) throws ValidationException
    {
        IgnoreDependentsFieldValidator delegate = new IgnoreDependentsFieldValidator();
        delegate.setFieldName(getFieldName());
        delegate.setNonIgnoreValues(enableSet);
        if (dependentFields.length > 0)
        {
            delegate.setDependentFields(dependentFields);
        }
        delegate.setValidationContext(getValidationContext());
        delegate.validateField(object);
    }
}
