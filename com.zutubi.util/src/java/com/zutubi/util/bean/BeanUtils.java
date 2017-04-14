/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.util.bean;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * A utility class for working with Java Beans.  See the
 * <a href="http://java.sun.com/products/javabeans/docs/index.html">JavaBeans specification</a>
 * for further details.
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
                throw new BeanPropertyException(String.format(
                        "No write method exists for property '%s'", propertyName)
                );
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
                throw new BeanPropertyException(String.format(
                        "Property '%s' does not have a getter on class %s.",
                        propertyName, target.getClass())
                );
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
