package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.TypeSqueezer;

/**
 * <class-comment/>
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

    public Object unsqueeze(String... str) throws SqueezeException
    {
        String value = str[0];
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
