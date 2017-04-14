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
 * Validates that numerical values fall within some range.
 */
public class NumericValidator extends FieldValidatorSupport
{
    public static final String MIN = "min";

    public static final String MAX = "max";

    private long max = Long.MAX_VALUE;
    
    private long min = Long.MIN_VALUE;

    public void setMax(long max)
    {
        this.max = max;
    }

    public void setMin(long min)
    {
        this.min = min;
    }

    public long getMax()
    {
        return max;
    }

    public long getMin()
    {
        return min;
    }

    public void validateField(Object value) throws ValidationException
    {
        if (value instanceof String)
        {
            try
            {
                value = Long.valueOf((String)value);
            }
            catch (NumberFormatException e)
            {
                addError();
                return;
            }
        }

        if (value instanceof Number)
        {
            Number number = (Number) value;
            if (isSet(number))
            {
                if (number.longValue() < min)
                {
                    addError(MIN);
                }
                if (max < number.longValue())
                {
                    addError(MAX);
                }
            }
        }
    }

    private boolean isSet(Number number)
    {
        if (number != null)
        {
            if (number instanceof Long)
            {
                return number.longValue() != Long.MIN_VALUE;
            }
            else if (number instanceof Integer)
            {
                return number.intValue() != Integer.MIN_VALUE;
            }
            else if (number instanceof Short)
            {
                return number.intValue() != Short.MIN_VALUE;
            }
            else if (number instanceof Byte)
            {
                return number.intValue() != Byte.MIN_VALUE;
            }
        }
        return false;
    }
}
