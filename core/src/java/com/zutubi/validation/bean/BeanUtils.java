package com.zutubi.validation.bean;

import java.beans.PropertyDescriptor;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.lang.reflect.Method;

/**
 * <class-comment/>
 */
public class BeanUtils
{
    private static final Object[] NO_ARGS = new Object[0];

    public static void setProperty(String propertyName, Object propertyValue, Object target) throws BeanException
    {
        try
        {
            PropertyDescriptor descriptor = getPropertyDescriptor(propertyName, target.getClass());

            Method writeMethod = descriptor.getWriteMethod();
            if (writeMethod == null)
            {
                throw new BeanPropertyException();
            }

            writeMethod.invoke(target, propertyValue);

        }
        catch (BeanException e)
        {
            // allow bean exceptions to pass through the catch.
            throw e;
        }
        catch (Exception e)
        {
            throw new BeanException(e);
        }
    }

    public static Object getProperty(String propertyName, Object target) throws BeanException
    {
        try
        {
            PropertyDescriptor descriptor = getPropertyDescriptor(propertyName, target.getClass());
            Method readMethod = descriptor.getReadMethod();
            if (readMethod == null)
            {
                throw new BeanPropertyException();
            }
            return readMethod.invoke(target, NO_ARGS);
        }
        catch (BeanException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new BeanException(e);
        }
    }

    public static PropertyDescriptor getPropertyDescriptor(String propertyName, Class target) throws BeanException
    {
        try
        {
            BeanInfo beanInfo = Introspector.getBeanInfo(target);
            for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors())
            {
                if (propertyName.equals(propertyDescriptor.getName()))
                {
                    return propertyDescriptor;
                }
            }
            throw new PropertyNotFoundException();
        }
        catch (IntrospectionException e)
        {
            throw new BeanException(e);
        }
    }

}
