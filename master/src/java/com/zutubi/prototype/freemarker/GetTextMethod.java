package com.zutubi.prototype.freemarker;

import freemarker.template.*;

import java.util.List;

import com.zutubi.i18n.Messages;
import com.opensymphony.util.TextUtils;

/**
 *
 *
 */
public class GetTextMethod implements TemplateMethodModel
{
    private Messages[] messages;

    public GetTextMethod(Messages... messages)
    {
        this.messages = messages;
    }

    public TemplateModel exec(List args) throws TemplateModelException
    {
        if (args.size() != 1)
        {
            throw new TemplateModelException("Wrong arguments");
        }
        String key = (String) args.get(0);

        String value = null;
        for (Messages message : messages)
        {
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