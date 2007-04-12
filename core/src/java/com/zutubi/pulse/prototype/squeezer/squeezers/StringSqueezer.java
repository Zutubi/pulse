package com.zutubi.pulse.prototype.squeezer.squeezers;

import com.zutubi.pulse.prototype.squeezer.TypeSqueezer;

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
