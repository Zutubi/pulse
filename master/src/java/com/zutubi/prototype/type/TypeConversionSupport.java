package com.zutubi.prototype.type;

import com.zutubi.pulse.form.squeezer.Squeezers;
import com.zutubi.pulse.form.squeezer.TypeSqueezer;

import java.lang.reflect.Method;
import java.util.Map;

/**
 *
 *
 */
public class TypeConversionSupport
{
    private TypeRegistry typeRegistry;

    public void applyToMap(Object instance, Map<String, Object> map) throws TypeException
    {
        CompositeType type = typeRegistry.getType(instance.getClass());
        if (type == null)
        {
            type = typeRegistry.register(instance.getClass());
        }
        for (TypeProperty property : type.getProperties(PrimitiveType.class))
        {
            try
            {
                TypeSqueezer squeezer = Squeezers.findSqueezer(property.getClazz());
                Object value = property.getGetter().invoke(instance);
                map.put(property.getName(), squeezer.squeeze(value));
            }
            catch (Exception e)
            {
                throw new TypeException(e);
            }
        }
    }

    public void applyMapTo(Map<String, Object> map, Object instance) throws TypeException
    {
        CompositeType type = typeRegistry.getType(instance.getClass());
        if (type == null)
        {
            type = typeRegistry.register(instance.getClass());
        }
        for (TypeProperty property : type.getProperties(PrimitiveType.class))
        {
            String propertyName = property.getName();
            try
            {
                Method setter = property.getSetter();
                if (setter != null) // is a writable field.
                {
                    Object value = map.get(propertyName);
                    TypeSqueezer squeezer = Squeezers.findSqueezer(property.getClazz());
                    if (squeezer == null)
                    {
                        throw new TypeConversionException("Unknown convertable type: " + property.getClazz());
                    }
                    
                    if (value instanceof String)
                    {
                        setter.invoke(instance, squeezer.unsqueeze((String)value));
                    }
                    else if (value instanceof String[])
                    {
                        setter.invoke(instance, squeezer.unsqueeze((String[])value));
                    }
                    else
                    {
                        setter.invoke(instance, squeezer.unsqueeze((String)value));
                    }
                }
            }
            catch (Exception e)
            {
                throw new TypeException(e);
            }
        }
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
