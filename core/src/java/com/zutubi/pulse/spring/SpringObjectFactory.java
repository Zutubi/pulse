package com.zutubi.pulse.spring;

import com.zutubi.util.bean.DefaultObjectFactory;

/**
 * An implementation of {@link com.zutubi.util.bean.ObjectFactory} that uses
 * the {@link SpringComponentContext} to wire the objects on creation.
 */
public class SpringObjectFactory extends DefaultObjectFactory
{
    public <T> T buildBean(Class<? extends T> clazz) throws Exception
    {
        return SpringComponentContext.createBean(clazz);
    }

    public <T> T buildBean(String className, Class<? super T> token) throws Exception
    {
        return buildBean(this.<T>getClassInstance(className, token));
    }

    public <T> T buildBean(Class<? extends T> clazz, Class[] argTypes, Object[] args) throws Exception
    {
        T object = super.buildBean(clazz, argTypes, args);
        SpringComponentContext.autowire(object);
        return object;
    }

    public <T> T buildBean(String className, Class<? super T> token, Class[] argTypes, Object[] args) throws Exception
    {
        return buildBean(this.<T>getClassInstance(className, token), argTypes, args);
    }
}
