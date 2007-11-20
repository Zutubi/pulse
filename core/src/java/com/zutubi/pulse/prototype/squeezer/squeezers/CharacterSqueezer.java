package com.zutubi.pulse.prototype.squeezer.squeezers;

import com.zutubi.util.TextUtils;
import com.zutubi.pulse.prototype.squeezer.SqueezeException;
import com.zutubi.pulse.prototype.squeezer.TypeSqueezer;

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
        String s = str[0];
        if(TextUtils.stringSet(s))
        {
            return s.charAt(0);
        }
        else
        {
            return null;
        }
    }
}
