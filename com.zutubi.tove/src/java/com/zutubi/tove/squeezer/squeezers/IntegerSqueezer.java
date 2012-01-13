package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.TypeSqueezer;
import com.zutubi.util.StringUtils;

/**
 * Converts between strings and integers, using Java's default integer parsing.
 *
 * @see Integer#parseInt(String)
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

    public Object unsqueeze(String s) throws SqueezeException
    {
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
