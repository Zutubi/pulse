package com.zutubi.pulse.prototype.squeezer.squeezers;

import com.zutubi.pulse.prototype.squeezer.SqueezeException;
import com.zutubi.pulse.prototype.squeezer.TypeSqueezer;

/**
 * <class-comment/>
 */
public class ByteSqueezer implements TypeSqueezer
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
        return Byte.parseByte(str[0]);
    }
}
