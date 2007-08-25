package com.zutubi.pulse.prototype.squeezer.squeezers;

import com.opensymphony.util.TextUtils;
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
        String s = str[0];
        if (TextUtils.stringSet(s))
        {
            try
            {
                return Byte.parseByte(s);
            }
            catch (NumberFormatException e)
            {
                throw new SqueezeException(String.format("'%s' is not a valid byte value", s));
            }
        }
        else
        {
            return null;
        }
    }
}
