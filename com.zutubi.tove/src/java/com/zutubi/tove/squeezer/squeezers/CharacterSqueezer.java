package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.TypeSqueezer;
import com.zutubi.util.StringUtils;

/**
 * Coverts between strings and characters.  Characters are encoded as strings
 * of length 1.
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

    public Object unsqueeze(String s) throws SqueezeException
    {
        if (StringUtils.stringSet(s))
        {
            return s.charAt(0);
        }
        else
        {
            return null;
        }
    }
}
