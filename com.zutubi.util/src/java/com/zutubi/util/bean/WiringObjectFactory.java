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

import com.zutubi.util.reflection.ReflectionUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * An object factory that can autowire properties based on the fields of some
 * object.  Useful in testing.
 */
public class WiringObjectFactory implements ObjectFactory
{
    private DefaultObjectFactory delegate = new DefaultObjectFactory();
    private Map<String, Object> properties = new HashMap<String, Object>();

    public void initProperties(Object fieldSource)
    {
        Class<?> sourceClass = fieldSource.getClass();

        for(Field field: ReflectionUtils.getDeclaredFields(sourceClass, Object.class))
        {
            field.setAccessible(true);
            try
            {
                properties.put(field.getName(), field.get(fieldSource));
            }
            catch (IllegalAccessException e)
            {
                // Oh well
            }
        }
    }

    public <T> T buildBean(Class<? extends T> clazz)
    {
        T bean = delegate.buildBean(clazz);
        wire(bean);
        return bean;
    }

    public <T> T buildBean(String className, Class<? super T> supertype)
    {
        // javac requires this type argument
        T bean = delegate.<T>buildBean(className, supertype);
        wire(bean);
        return bean;
    }

    public <T> T buildBean(Class<? extends T> clazz, Object... args)
    {
        T bean = delegate.buildBean(clazz, args);
        wire(bean);
        return bean;
    }

    public <T> T buildBean(Class<? extends T> clazz, Class[] argTypes, Object[] args)
    {
        T bean = delegate.buildBean(clazz, argTypes, args);
        wire(bean);
        return bean;
    }

    public <T> T buildBean(String className, Class<? super T> supertype, Class[] argTypes, Object[] args)
    {
        // javac requires this type argument
        T bean = delegate.<T>buildBean(className, supertype, argTypes, args);
        wire(bean);
        return bean;
    }

    public <T> Class<? extends T> getClassInstance(String className, Class<? super T> supertype)
    {
        // javac requires this type argument
        return delegate.<T>getClassInstance(className, supertype);
    }

    private void wire(Object bean)
    {
        try
        {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass());
            for(PropertyDescriptor pd: beanInfo.getPropertyDescriptors())
            {
                Object value = properties.get(pd.getName());
                if (value != null)
                {
                    Method writeMethod = pd.getWriteMethod();
                    if(writeMethod != null)
                    {
                        writeMethod.invoke(bean, value);
                    }
                }
            }
        }
        catch (Exception e)
        {
            // We tried
        }
    }
}
