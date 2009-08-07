package com.zutubi.tove.squeezer.squeezers;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.TypeSqueezer;
import com.zutubi.util.StringUtils;

/**
 * A type squeezer that converts to and from enums.  Assumes a naming
 * convention of all upper case words separated by underscores to convert
 * symbolic names to and from human-readable strings.
 */
public class EnumSqueezer implements TypeSqueezer
{
    private Class<? extends Enum> enumClass;

    public EnumSqueezer(Class<? extends Enum> enumClass)
    {
        this.enumClass = enumClass;
    }

    public String squeeze(Object obj) throws SqueezeException
    {
        if (obj == null)
        {
            return "";
        }
        return obj.toString().toLowerCase().replace('_', ' ');
    }

    public Object unsqueeze(String... str) throws SqueezeException
    {
        String s = str[0];
        if (!StringUtils.stringSet(s))
        {
            return null;
        }

        String converted = s.toUpperCase().replace(' ', '_');
        try
        {
            return Enum.valueOf(enumClass, converted);
        }
        catch (IllegalArgumentException e)
        {
            throw new SqueezeException("Invalid value '" + s + "'");
        }
    }
}
