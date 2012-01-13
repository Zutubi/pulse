package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.TypeSqueezer;
import com.zutubi.util.StringUtils;

/**
 * Converts between strings and floats, using Java's default float parsing.
 *
 * @see Float#parseFloat(String)
 */
public class FloatSqueezer implements TypeSqueezer
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
            return Float.parseFloat(s);
        }
        catch (NumberFormatException e)
        {
            throw new SqueezeException(String.format("'%s' is not a valid float", s));
        }
    }
}
