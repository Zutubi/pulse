package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.TypeSqueezer;

/**
 * An identity conversion for strings (almost: null converts to the empty
 * string).
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

    public Object unsqueeze(String s)
    {
        return s;
    }
}
