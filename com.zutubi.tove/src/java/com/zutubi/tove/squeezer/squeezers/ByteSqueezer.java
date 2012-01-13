package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.TypeSqueezer;
import com.zutubi.util.StringUtils;

/**
 * Converts between bytes and strings.  Bytes are formatted as signed decimal strings.
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

    public Object unsqueeze(String s) throws SqueezeException
    {
        if (StringUtils.stringSet(s))
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
