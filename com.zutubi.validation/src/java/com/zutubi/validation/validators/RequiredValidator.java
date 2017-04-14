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

import com.zutubi.validation.ValidationException;

import java.util.Collection;

/**
 * Used to ensure a field has a non-empty value.
 */
public class RequiredValidator extends FieldValidatorSupport
{
    public RequiredValidator()
    {
        super("required");
    }

    public void validateField(Object fieldValue) throws ValidationException
    {
        if (!isValueSet(fieldValue))
        {
            addError();
        }
    }

    public static boolean isValueSet(Object fieldValue)
    {
        if (fieldValue == null)
        {
            return false;
        }

        if (fieldValue instanceof String)
        {
            String str = ((String)fieldValue);
            if (str.length() == 0)
            {
                return false;
            }
        }
        else if (fieldValue instanceof Collection)
        {
            Collection c = (Collection) fieldValue;
            if (c.size() == 0)
            {
                return false;
            }
        }

        return true;
    }
}
