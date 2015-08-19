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
    private Configuration instance;
    private String key;

    public ValidationException(Configuration instance)
    {
        this(instance, null);
    }

    public ValidationException(Configuration instance, String key)
    {
        super("Validation failed");
        this.instance = instance;
        this.key = key;
    }

    public Error getError()
    {
        return new Error(this, instance);
    }

    public static class Error extends ApiExceptionHandler.Error
    {
        private final List<String> instanceErrors;
        private final Map<String, List<String>> fieldErrors;
        private final String key;

        public Error(ValidationException ex, Configuration instance)
        {
            super(ex);
            instanceErrors = new ArrayList<>(instance.getInstanceErrors());
            fieldErrors = new HashMap<>(instance.getFieldErrors());
            key = ex.key;
        }

        public List<String> getInstanceErrors()
        {
            return instanceErrors;
        }

        public Map<String, List<String>> getFieldErrors()
        {
            return fieldErrors;
        }

        public String getKey()
        {
            return key;
        }
    }
}
