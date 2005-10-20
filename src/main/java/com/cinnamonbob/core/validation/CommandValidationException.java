package com.cinnamonbob.core.validation;

import com.opensymphony.xwork.validator.ValidatorContext;

import java.util.Map;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jsankey
 * Date: 20/10/2005
 * Time: 22:05:47
 * To change this template use File | Settings | File Templates.
 */
public class CommandValidationException extends Exception
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

        //Map fieldErrors = context.getFieldErrors();
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
