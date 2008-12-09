package com.zutubi.util.bean;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 */
public class BeanUtils
{
    public static void setProperty(String propertyName, Object propertyValue, Object target) throws BeanException
    {
        try
        {
            PropertyDescriptor descriptor = getPropertyDescriptor(propertyName, target.getClass());

            Method writeMethod = descriptor.getWriteMethod();
            if (writeMethod == null)
            {
                throw new BeanPropertyException("No write method exists for property '" + propertyName + "'");
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
                throw new BeanPropertyException("property does not have a getter.");
            }
            return readMethod.invoke(target);
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
            throw new PropertyNotFoundException(String.format("Property %s not found in target class %s", propertyName, target));
        }
        catch (IntrospectionException e)
        {
            throw new BeanException(e);
        }
    }
}
