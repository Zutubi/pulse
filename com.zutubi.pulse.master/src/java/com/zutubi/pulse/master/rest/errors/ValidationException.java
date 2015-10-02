package com.zutubi.pulse.master.rest.errors;

import com.zutubi.tove.config.api.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a validation failure of a composite instance on insert/update.
 */
public class ValidationException extends RuntimeException
{
    private final Error error;

    public ValidationException()
    {
        super("Validation failed");
        this.error = new Error(this);
    }

    public ValidationException(Configuration instance)
    {
        this(instance, null);
    }

    public ValidationException(Configuration instance, String key)
    {
        super("Validation failed");
        this.error = new Error(this, instance, key);
    }

    public void addFieldError(String field, String message)
    {
        error.addFieldError(field, message);
    }

    public Error getError()
    {
        return error;
    }

    public static class Error extends ApiExceptionHandler.Error
    {
        private final List<String> instanceErrors;
        private final Map<String, List<String>> fieldErrors;
        private final String key;

        public Error(ValidationException ex)
        {
            super(ex);
            instanceErrors = new ArrayList<>();
            fieldErrors = new HashMap<>();
            key = null;
        }

        public Error(ValidationException ex, Configuration instance, String key)
        {
            super(ex);
            instanceErrors = new ArrayList<>(instance.getInstanceErrors());
            fieldErrors = new HashMap<>(instance.getFieldErrors());
            this.key = key;
        }

        public List<String> getInstanceErrors()
        {
            return instanceErrors;
        }

        public Map<String, List<String>> getFieldErrors()
        {
            return fieldErrors;
        }

        public void addFieldError(String field, String message)
        {
            List<String> errors = fieldErrors.get(field);
            if (errors == null)
            {
                errors = new ArrayList<>();
                fieldErrors.put(field, errors);
            }

            errors.add(message);
        }

        public String getKey()
        {
            return key;
        }
    }
}
