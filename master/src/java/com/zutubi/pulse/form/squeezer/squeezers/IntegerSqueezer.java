package com.zutubi.pulse.form.squeezer.squeezers;

import com.zutubi.pulse.form.squeezer.TypeSqueezer;
import com.zutubi.pulse.form.squeezer.SqueezeException;
import com.opensymphony.util.TextUtils;

/**
 * <class-comment/>
 */
public class IntegerSqueezer implements TypeSqueezer
{
    public String squeeze(Object obj) throws SqueezeException
    {
        if (obj == null)
        {
            return "";
        }
        return obj.toString();
    }

    public Object unsqueeze(String... str) throws SqueezeException
    {
        try
        {
            if (TextUtils.stringSet(str[0]))
            {
                return Integer.parseInt(str[0]);
            }
        }
        catch (NumberFormatException e)
        {
            throw new SqueezeException(e.getMessage());
        }
        return null;
    }
}
