package com.zutubi.prototype.type;

import com.zutubi.pulse.form.squeezer.Squeezers;
import com.zutubi.pulse.form.squeezer.TypeSqueezer;

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
        CompositeType type = (CompositeType) typeRegistry.getType(instance.getClass());
        if (type == null)
        {
            type = typeRegistry.register(instance.getClass());
        }
        for (String propertyName : type.getProperties(PrimitiveType.class))
        {
            try
            {
                TypeProperty property = type.getProperty(propertyName);
                TypeSqueezer squeezer = Squeezers.findSqueezer(property.getClazz());
                Object value = property.getGetter().invoke(instance);
                map.put(propertyName, squeezer.squeeze(value));
            }
            catch (Exception e)
            {
                throw new TypeException(e);
            }
        }
    }

    public void applyMapTo(Map<String, Object> map, Object instance) throws TypeException
    {
        CompositeType type = (CompositeType) typeRegistry.getType(instance.getClass());
        if (type == null)
        {
            type = typeRegistry.register(instance.getClass());
        }
        for (String propertyName : type.getProperties(PrimitiveType.class))
        {
            if (!map.containsKey(propertyName))
            {
                continue;
            }
            try
            {
                Object value = map.get(propertyName);

                TypeProperty property = type.getProperty(propertyName);
                TypeSqueezer squeezer = Squeezers.findSqueezer(property.getClazz());

                if (value instanceof String)
                {
                    property.getSetter().invoke(instance, squeezer.unsqueeze((String)value));
                }
                else if (value instanceof String[])
                {
                    property.getSetter().invoke(instance, squeezer.unsqueeze((String[])value));
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
