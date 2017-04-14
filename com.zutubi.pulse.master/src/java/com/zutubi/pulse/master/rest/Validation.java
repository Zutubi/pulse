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

package com.zutubi.pulse.master.rest;

import com.zutubi.pulse.master.rest.errors.ValidationException;

import java.util.Map;

/**
 * Helpers for common validation tasks in the remote API.
 */
public class Validation
{
    public static ValidationException newFieldError(String field, String message) throws ValidationException
    {
        ValidationException e = new ValidationException();
        e.addFieldError(field, message);
        return e;
    }

    public static String getRequiredString(String field, String label, Map<String, Object> input) throws ValidationException
    {
        Object value = input.get(field);
        if (value == null)
        {
            throw Validation.newFieldError(field, label + " is required");
        }
        else if (!(value instanceof String))
        {
            throw Validation.newFieldError(field, "Unexpected type for field " + label + ": expected string, got " + value.getClass().getName());
        }
        else
        {
            String s = (String) value;
            if (s.length() == 0)
            {
                throw Validation.newFieldError(field, label + " is required");
            }

            return s;
        }
    }

    public static boolean getBoolean(String field, String label, Map<String, Object> input, boolean defaultValue)
    {
        Object value = input.get(field);
        if (value == null)
        {
            return defaultValue;
        }
        else if (!(value instanceof Boolean))
        {
            throw Validation.newFieldError(field, "Unexpected type for field " + label + ": expected boolean, got " + value.getClass().getName());
        }
        else
        {
            return (Boolean) value;
        }
    }
}
