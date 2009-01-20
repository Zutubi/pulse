package com.zutubi.pulse.core.spring;

import com.zutubi.util.bean.DefaultObjectFactory;

/**
 * An implementation of {@link com.zutubi.util.bean.ObjectFactory} that uses
 * the {@link SpringComponentContext} to wire the objects on creation.
 */
public class SpringObjectFactory extends DefaultObjectFactory
{
    public <T> T buildBean(Class<? extends T> clazz)
    {
        T object = super.buildBean(clazz);
        SpringComponentContext.autowire(object);
        return object;
    }

    public <T> T buildBean(String className, Class<? super T> token)
    {
        return buildBean(this.<T>getClassInstance(className, token));
    }

    public <T> T buildBean(Class<? extends T> clazz, Class[] argTypes, Object[] args)
    {
        T object = super.buildBean(clazz, argTypes, args);
        SpringComponentContext.autowire(object);
        return object;
    }

    public <T> T buildBean(String className, Class<? super T> token, Class[] argTypes, Object[] args)
    {
        return buildBean(this.<T>getClassInstance(className, token), argTypes, args);
    }
}
