package com.zutubi.prototype.type;

import com.zutubi.prototype.config.InstanceCache;
import com.zutubi.pulse.prototype.squeezer.SqueezeException;
import com.zutubi.pulse.prototype.squeezer.Squeezers;
import com.zutubi.pulse.prototype.squeezer.TypeSqueezer;

/**
 *
 *
 */
public class PrimitiveType extends SimpleType implements Type
{
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

    public Object instantiate(String path, InstanceCache cache, Object data) throws TypeException
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
}
