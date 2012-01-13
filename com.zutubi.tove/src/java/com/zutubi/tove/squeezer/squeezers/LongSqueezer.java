package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.TypeSqueezer;
import com.zutubi.util.StringUtils;

/**
 * Converts between strings and longs, using Java's default long parsing.
 *
 * @see Long#parseLong(String)
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

    public Object unsqueeze(String s) throws SqueezeException
    {
        if (!StringUtils.stringSet(s))
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
