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

/**
 * <class-comment/>
 */
public abstract class AbstractRangeValidator<T extends Comparable> extends FieldValidatorSupport
{
    @SuppressWarnings({"unchecked"})
    public void validateField(Object obj) throws ValidationException
    {
        // if there is no value - don't do comparison. If a value is required, a required validator should be
        // added to the field
        if (obj == null)
        {
            return;
        }

        if (!(obj instanceof Comparable))
        {
            throw new ValidationException();
        }

        Comparable value = (Comparable) obj;

        // only check for a minimum value if the min parameter is set
        if ((getMinComparatorValue() != null) && (value.compareTo(getMinComparatorValue()) < 0))
        {
            addError(".min");
        }

        // only check for a maximum value if the max parameter is set
        if ((getMaxComparatorValue() != null) && (value.compareTo(getMaxComparatorValue()) > 0))
        {
            addError(".max");
        }
    }

    protected abstract T getMaxComparatorValue();
    protected abstract T getMinComparatorValue();
}
