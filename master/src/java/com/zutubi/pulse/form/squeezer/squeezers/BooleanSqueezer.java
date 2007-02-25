package com.zutubi.pulse.form.squeezer.squeezers;

import com.zutubi.pulse.form.squeezer.TypeSqueezer;
import com.zutubi.pulse.form.squeezer.SqueezeException;

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
