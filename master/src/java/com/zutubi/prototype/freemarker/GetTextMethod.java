package com.zutubi.prototype.freemarker;

import com.opensymphony.util.TextUtils;
import com.zutubi.i18n.Messages;
import com.zutubi.prototype.type.Type;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.*;

import java.util.List;

/**
 * A freemarker method model that uses our i18n code to resolve a key.  Three
 * overloads are avaialble:
 *
 * (String key):
 *     resolves the key using the messages that have been set globally on
 *     construction of this method object
 * (Type type, String key):
 *     resolves the key using messages derived by using the Type's class as a
 *     context
 * (Object context, String key):
 *     resolves the key using messages derived by using the given context
 */
public class GetTextMethod implements TemplateMethodModelEx
{
    private Messages[] messages;

    /**
     * Creates a new method with the given global messages.
     *
     * @param messages messages used to resolve keys when no specific context
     *                 is provided
     */
    public GetTextMethod(Messages... messages)
    {
        this.messages = messages;
    }

    public TemplateModel exec(List args) throws TemplateModelException
    {
        int argCount = args.size();
        if (argCount == 1)
        {
            // Arg should be a string key
            if(!(args.get(0) instanceof SimpleScalar))
            {
                throw new TemplateModelException("Single argument must be a string key");
            }

            return getText(((SimpleScalar) args.get(0)).getAsString(), messages);
        }
        else if(argCount == 2)
        {
            if(!(args.get(0) instanceof BeanModel))
            {
                throw new TemplateModelException("First argument must be a wrapped object");
            }
            if(!(args.get(1) instanceof SimpleScalar))
            {
                throw new TemplateModelException("Second argument must be a string key");
            }

            Object context = ((BeansWrapper)ObjectWrapper.BEANS_WRAPPER).unwrap((TemplateModel) args.get(0));
            if(context instanceof Type)
            {
                // args are a type that forms the context and the string key
                return getText(((SimpleScalar) args.get(1)).getAsString(), Messages.getInstance(((Type)context).getClazz()));
            }
            else
            {
                // first arg is context, second is string key
                return getText(((SimpleScalar) args.get(1)).getAsString(), Messages.getInstance(context));
            }
        }
        else
        {
            throw new TemplateModelException("Wrong arguments");
        }
    }

    private TemplateModel getText(String key, Messages... messages)
    {
        String value = null;
        for (Messages message : messages)
        {
            if (message == null)
            {
                continue;
            }
            value = message.format(key);
            if (TextUtils.stringSet(value))
            {
                break;
            }
        }

        if (!TextUtils.stringSet(value))
        {
            value = key;
        }
        return new SimpleScalar(value);
    }
}