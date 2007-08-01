package com.zutubi.prototype.type;

import com.zutubi.pulse.prototype.squeezer.SqueezeException;
import com.zutubi.pulse.prototype.squeezer.Squeezers;
import com.zutubi.pulse.prototype.squeezer.TypeSqueezer;
import com.zutubi.util.CollectionUtils;

/**
 * Manages basic numerical, boolean and string values.
 */
public class PrimitiveType extends SimpleType implements Type
{
    private static final Class[] XML_RPC_TYPES = { Boolean.class, Double.class, Integer.class, String.class };

    public PrimitiveType(Class type)
    {
        this(type, null);
    }

    public PrimitiveType(Class type, String symbolicName)
    {
        super(type, symbolicName);
        if (Squeezers.findSqueezer(type) == null)
        {
            throw new IllegalArgumentException("Unsupported primitive type: " + type);
        }
    }

    public Object instantiate(Object data, Instantiator instantiator) throws TypeException
    {
        TypeSqueezer squeezer = Squeezers.findSqueezer(getClazz());
        try
        {
            if (data instanceof String[])
            {
                return squeezer.unsqueeze((String[]) data);
            }
            else if (data instanceof String)
            {
                return squeezer.unsqueeze((String) data);
            }
            return data;
        }
        catch (SqueezeException e)
        {
            throw new TypeConversionException(e.getMessage());
        }
    }

    public Object unstantiate(Object instance) throws TypeException
    {
        TypeSqueezer squeezer = Squeezers.findSqueezer(getClazz());
        try
        {
            return squeezer.squeeze(instance);
        }
        catch (SqueezeException e)
        {
            throw new TypeException(e);
        }
    }

    public Object toXmlRpc(Object data) throws TypeException
    {
        if(data == null)
        {
            return null;
        }

        // XML-RPC only supports limited types, in their direct form.
        Class clazz = getClazz();
        String s = (String) data;
        if(CollectionUtils.contains(XML_RPC_TYPES, clazz))
        {
            return instantiate(s, null);
        }
        else if(clazz == Byte.class)
        {
            // Convert up to int
            return Byte.valueOf(s).intValue();
        }
        else if(clazz == Float.class)
        {
            // Convert up to double
            return Float.valueOf(s).doubleValue();
        }
        else if(clazz == Short.class)
        {
            // Convert up to int
            return Short.valueOf(s).intValue();
        }
        else
        {
            // Leave as a string.  This includes characters, and unfortunately
            // longs as well (XML-RPC has no direct way to specify a 64 bit
            // int).
            return s;
        }
    }
}
