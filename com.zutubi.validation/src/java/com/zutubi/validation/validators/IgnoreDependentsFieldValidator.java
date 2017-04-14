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
