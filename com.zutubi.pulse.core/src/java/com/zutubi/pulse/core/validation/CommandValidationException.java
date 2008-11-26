package com.zutubi.pulse.core.validation;

import com.zutubi.pulse.core.engine.api.FileLoadException;
import com.zutubi.validation.ValidationContext;

import java.util.List;

/**
 */
public class CommandValidationException extends FileLoadException
{
    private ValidationContext context;

    public CommandValidationException(ValidationContext context)
    {
        this.context = context;
    }

    public ValidationContext getContext()
    {
        return context;
    }

    @Override
    public String getMessage()
    {
        StringBuilder builder = new StringBuilder(1024);

        for(Object error: context.getActionErrors())
        {
            builder.append(error);
            builder.append('\n');
        }

        for(List<String> errorList: context.getFieldErrors().values())
        {
            for(String error: errorList)
            {
                builder.append(error);
                builder.append('\n');
            }
        }

        return builder.toString();
    }
}
