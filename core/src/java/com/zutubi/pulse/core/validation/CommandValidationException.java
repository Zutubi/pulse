/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.core.validation;

import com.opensymphony.xwork.validator.ValidatorContext;
import com.zutubi.pulse.core.FileLoadException;

import java.util.List;

/**
 */
public class CommandValidationException extends FileLoadException
{
    private ValidatorContext context;

    public CommandValidationException(ValidatorContext context)
    {
        this.context = context;
    }

    public ValidatorContext getContext()
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

        for(Object errorList: context.getFieldErrors().values())
        {
            for(Object error: ((List)errorList))
            {
                builder.append(error);
                builder.append('\n');
            }
        }

        return builder.toString();
    }
}
