package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.TypeSqueezer;

/**
 * Converts between strings and booleans.  In addition to the standard Java
 * behaviour (a string set to "true", case ignored, is true) this converter
 * also recognises "on" and "yes" (again, case ignored) as true.  Everythig
 * else is false.
 */
public class BooleanSqueezer implements TypeSqueezer
{
    public String squeeze(Object obj) throws SqueezeException
    {
        if (obj == null)
        {
            return "";
        }
        return obj.toString();
    }

    public Boolean unsqueeze(String value) throws SqueezeException
    {
        if (value != null)
        {
            if ("on".equalsIgnoreCase(value))
            {
                return Boolean.TRUE;
            }
            if ("yes".equalsIgnoreCase(value))
            {
                return Boolean.TRUE;
            }
        }
        return Boolean.parseBoolean(value);
    }
}
