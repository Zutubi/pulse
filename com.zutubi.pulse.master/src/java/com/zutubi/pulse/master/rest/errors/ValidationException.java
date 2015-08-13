package com.zutubi.pulse.master.rest.errors;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a validation failure of a composite instance on insert/update.
 */
public class ValidationException extends RuntimeException
{
    private CompositeType type;
    private Configuration instance;

    public ValidationException(CompositeType type, Configuration instance)
    {
        super("Validation failed");
        this.type = type;
        this.instance = instance;
    }

    public Error getError()
    {
        return new Error(this, instance);
    }

    public static class Error extends ApiExceptionHandler.Error
    {
        private List<String> instanceErrors;
        private Map<String, List<String>> fieldErrors;

        public Error(ValidationException ex, Configuration instance)
        {
            super(ex);
            instanceErrors = new ArrayList<>(instance.getInstanceErrors());
            fieldErrors = new HashMap<>(instance.getFieldErrors());
        }

        public List<String> getInstanceErrors()
        {
            return instanceErrors;
        }

        public Map<String, List<String>> getFieldErrors()
        {
            return fieldErrors;
        }
    }
}
