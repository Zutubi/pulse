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
