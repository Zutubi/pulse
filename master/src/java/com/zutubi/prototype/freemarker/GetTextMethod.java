package com.zutubi.prototype.freemarker;

import freemarker.template.*;

import java.util.List;

/**
 *
 *
 */
public class GetTextMethod implements TemplateMethodModel
{
    public TemplateModel exec(List args) throws TemplateModelException
    {
        if (args.size() != 1)
        {
            throw new TemplateModelException("Wrong arguments");
        }
        return new SimpleScalar((String) args.get(0));
    }
}