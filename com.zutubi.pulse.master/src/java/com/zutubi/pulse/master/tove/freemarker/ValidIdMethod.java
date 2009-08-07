package com.zutubi.pulse.master.tove.freemarker;

import com.zutubi.util.WebUtils;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.List;

/**
 * A freemarker method model that converts a string to a valid HTML name for
 * use in IDs.
 */
public class ValidIdMethod implements TemplateMethodModelEx
{
    public TemplateModel exec(List args) throws TemplateModelException
    {
        int argCount = args.size();
        if (argCount == 1)
        {
            // Arg should be a string key
            Object arg = args.get(0);
            if(arg instanceof SimpleScalar)
            {
                return new SimpleScalar(WebUtils.toValidHtmlName(((SimpleScalar) args.get(0)).getAsString()));
            }
            else
            {
                throw new TemplateModelException("Unexpected argument model type '" + arg.getClass().getName() + "': expecting SimpleScalar");
            }
        }
        else
        {
            throw new TemplateModelException("Unexpected number arguments: expecting 1, got " + argCount);
        }
    }
}
