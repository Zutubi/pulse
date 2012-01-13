package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.TypeSqueezer;
import com.zutubi.util.StringUtils;

/**
 * Converts between strings and doubles using Java's default double parsing.
 *
 * @see Double#parseDouble(String)
 */
public class DoubleSqueezer implements TypeSqueezer
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
            return Double.parseDouble(s);
        }
        catch (NumberFormatException e)
        {
            throw new SqueezeException(String.format("'%s' is not a valid double", s));
        }
    }
}
