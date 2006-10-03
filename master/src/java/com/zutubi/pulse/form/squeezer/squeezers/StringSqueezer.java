package com.zutubi.pulse.form.squeezer.squeezers;

import com.zutubi.pulse.form.squeezer.TypeSqueezer;

/**
 * <class-comment/>
 */
public class StringSqueezer implements TypeSqueezer
{
    public String squeeze(Object obj)
    {
        if (obj == null)
        {
            return "";
        }
        return (String)obj;
    }

    public Object unsqueeze(String... str)
    {
        return str[0];
    }
}
