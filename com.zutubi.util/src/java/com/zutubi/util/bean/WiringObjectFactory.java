package com.zutubi.util.bean;

import com.zutubi.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.HashMap;
import java.beans.Introspector;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;

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
        Class<? extends Object> sourceClass = fieldSource.getClass();

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

    public <V> V buildBean(Class<V> clazz) throws Exception
    {
        V bean = delegate.buildBean(clazz);
        wire(bean);
        return bean;
    }

    public <U> U buildBean(String className) throws Exception
    {
        U bean = (U)delegate.buildBean(className);
        wire(bean);
        return bean;
    }

    public <W> W buildBean(Class<W> clazz, Class[] argTypes, Object[] args) throws Exception
    {
        W bean = delegate.buildBean(clazz, argTypes, args);
        wire(bean);
        return bean;
    }

    public <X> X buildBean(String className, Class[] argTypes, Object[] args) throws Exception
    {
        X bean = (X)delegate.buildBean(className, argTypes, args);
        wire(bean);
        return bean;
    }

    public Class getClassInstance(String className) throws ClassNotFoundException
    {
        return delegate.getClassInstance(className);
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
