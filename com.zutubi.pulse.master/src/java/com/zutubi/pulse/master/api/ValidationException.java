package com.zutubi.pulse.master.api;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.TypeException;
import com.zutubi.util.GraphFunction;
import com.zutubi.util.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Collapses all validation error messages found on a {@link Configuration}
 * instance down to a single error message for transport over XML-RPC as a
 * simple fault.
 */
public class ValidationException extends Exception
{
    private static final Logger LOG = Logger.getLogger(ValidationException.class);

    private String message;

    public ValidationException(ComplexType type, Object instance)
    {
        MessageAccumulator messageAccumulator = new MessageAccumulator();
        try
        {
            type.forEachComplex(instance, messageAccumulator);
            message = messageAccumulator.message.toString();
        }
        catch (TypeException e)
        {
            LOG.severe(e);
            message = "Unable to determine validation errors: " + e.getMessage();
        }
    }

    public String getMessage()
    {
        return message;
    }

    private static class MessageAccumulator implements GraphFunction<Object>
    {
        private StringBuilder message = new StringBuilder();
        private Stack<String> context = new Stack<String>();

        public boolean push(String edge)
        {
            context.push(edge);
            return true;
        }

        public void process(Object o)
        {
            if(o instanceof Configuration)
            {
                Configuration c = (Configuration) o;
                for(String error: c.getInstanceErrors())
                {
                    addError(null, error);
                }

                for(Map.Entry<String, List<String>> fieldErrors: c.getFieldErrors().entrySet())
                {
                    List<String> messages = fieldErrors.getValue();
                    for(String error: messages)
                    {
                        addError(fieldErrors.getKey(), error);
                    }
                }
            }
        }

        public void pop()
        {
            context.pop();
        }

        private void addError(String field, String error)
        {
            if(message.length() > 0)
            {
                message.append("\n");
            }

            boolean first = true;
            for(String edge: context)
            {
                if(first)
                {
                    first = false;
                }
                else
                {
                    message.append('.');
                }

                message.append(edge);
            }

            if(field != null)
            {
                if(first)
                {
                    first = false;
                }
                else
                {
                    message.append('.');
                }

                message.append(field);
            }

            if(!first)
            {
                message.append(": ");
            }

            message.append(error);
        }

    }
}
