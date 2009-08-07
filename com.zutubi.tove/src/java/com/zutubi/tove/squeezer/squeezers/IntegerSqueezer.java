package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.TypeSqueezer;
import com.zutubi.util.StringUtils;

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
        String s = str[0];
        if (!StringUtils.stringSet(s))
        {
            return null;
        }
        try
        {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e)
        {
            throw new SqueezeException(String.format("'%s' is not a valid integer", s));
        }
    }
}
