package com.zutubi.pulse.spring;

import com.zutubi.util.bean.DefaultObjectFactory;

/**
 * The spring object factory is an implementation of the ObjectFactory interface that
 * uses the SpringComponentContext to wire the objects on creation.
 *  
 */
public class SpringObjectFactory extends DefaultObjectFactory
{
    public <W> W buildBean(Class<W> clazz) throws Exception
    {
        return SpringComponentContext.createBean(clazz);
    }

    public <V> V buildBean(String className) throws Exception
    {
        return (V) buildBean(getClassInstance(className));
    }

    public <V> V buildBean(Class<V> clazz, Class[] argTypes, Object[] args) throws Exception
    {
        V object = super.buildBean(clazz, argTypes, args);
        SpringComponentContext.autowire(object);
        return object;
    }

    public <V> V buildBean(String className, Class[] argTypes, Object[] args) throws Exception
    {
        return (V) buildBean(getClassInstance(className), argTypes, args);
    }
}
