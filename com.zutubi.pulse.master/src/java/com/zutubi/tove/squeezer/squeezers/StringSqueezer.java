package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.TypeSqueezer;

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
