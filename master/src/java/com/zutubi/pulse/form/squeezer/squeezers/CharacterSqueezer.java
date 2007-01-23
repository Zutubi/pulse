package com.zutubi.pulse.form.squeezer.squeezers;

import com.zutubi.pulse.form.squeezer.SqueezeException;
import com.zutubi.pulse.form.squeezer.TypeSqueezer;

/**
 * <class-comment/>
 */
public class CharacterSqueezer implements TypeSqueezer
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
        return str[0].charAt(0);
    }
}
