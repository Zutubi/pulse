package com.zutubi.validation.providers;

import com.zutubi.validation.ValidatorProvider;
import com.zutubi.validation.Validator;
import com.zutubi.pulse.util.CollectionUtils;
import com.zutubi.pulse.core.ObjectFactory;

import java.util.List;
import java.beans.PropertyDescriptor;
import java.beans.Introspector;
import java.beans.IntrospectionException;

/**
 *
 *
 */
public class PrimitiveTypeValidationProvider implements ValidatorProvider
{
    private static final Class[] PRIMITIVE_TYPES = {Boolean.class, Boolean.TYPE, Byte.class, Byte.TYPE, Character.class, Character.TYPE, Double.class, Double.TYPE, Float.class, Float.TYPE, Integer.class, Integer.TYPE, Long.class, Long.TYPE, Short.class, Short.TYPE};

    private ObjectFactory objectFactory = null;

    public List<Validator> getValidators(Object obj)
    {
        try
        {
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(obj.getClass(), Object.class).getPropertyDescriptors())
            {
                Class<?> propertyType = descriptor.getPropertyType();
                if (isPrimitive(propertyType))
                {

                }
            }
        }
        catch (IntrospectionException e)
        {
            // noop.
        }
        return null;
    }

    public static boolean isPrimitive(Class type)
    {
        return CollectionUtils.containsIdentity(PRIMITIVE_TYPES, type);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
