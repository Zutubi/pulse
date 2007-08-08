package com.zutubi.pulse.api;

import com.zutubi.pulse.core.config.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Collapses all validation error messages found on a {@link Configuration}
 * instance down to a single error message for transport over XML-RPC as a
 * simple fault.
 */
public class ValidationException extends Exception
{
    private String message;

    public ValidationException(Configuration instance)
    {
        StringBuilder message = new StringBuilder();
        for(String error: instance.getInstanceErrors())
        {
            addError(message, error);
        }

        for(Map.Entry<String, List<String>> fieldErrors: instance.getFieldErrors().entrySet())
        {
            addError(message, "Field ", fieldErrors.getKey(), ":");
            for(String error: fieldErrors.getValue())
            {
                addError(message, "  ", error);
            }
        }

        this.message = message.toString();
    }

    private void addError(StringBuilder message, String... pieces)
    {
        if(message.length() > 0)
        {
            message.append("\n");
        }

        for (String error: pieces)
        {
            message.append(error);
        }
    }

    public String getMessage()
    {
        return message;
    }
}
