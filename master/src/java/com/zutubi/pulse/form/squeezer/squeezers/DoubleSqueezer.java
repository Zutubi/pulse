package com.zutubi.pulse.form.squeezer.squeezers;

import com.zutubi.pulse.form.squeezer.TypeSqueezer;
import com.zutubi.pulse.form.squeezer.SqueezeException;

/**
 * <class-comment/>
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

    public Object unsqueeze(String... str) throws SqueezeException
    {
        return Double.parseDouble(str[0]);
    }
}
