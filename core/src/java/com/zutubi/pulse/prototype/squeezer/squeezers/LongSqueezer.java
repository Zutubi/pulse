package com.zutubi.pulse.prototype.squeezer.squeezers;

import com.zutubi.pulse.prototype.squeezer.TypeSqueezer;
import com.zutubi.pulse.prototype.squeezer.SqueezeException;
import com.opensymphony.util.TextUtils;

/**
 * <class-comment/>
 */
public class LongSqueezer implements TypeSqueezer
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
        String s = str[0];
        if (!TextUtils.stringSet(s))
        {
            return null;
        }
        try
        {
            return Long.parseLong(s);
        }
        catch (NumberFormatException e)
        {
            throw new SqueezeException(String.format("'%s' is not a valid long", s));
        }
    }
}
