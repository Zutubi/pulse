package com.zutubi.pulse.spring;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.ObjectFactory;

/**
 * 
 */
public class SpringObjectFactory extends ObjectFactory
{
    public <W> W buildBean(Class clazz) throws Exception
    {
        return (W) ComponentContext.createBean(clazz);
    }

    public <V> V buildBean(String className) throws Exception
    {
        return (V) buildBean(getClassInstance(className));
    }

    public <V> V buildBean(Class clazz, Class[] argTypes, Object[] args) throws Exception
    {
        Object object = super.buildBean(clazz, argTypes, args);
        ComponentContext.autowire(object);
        return (V)object;
    }

    public <V> V buildBean(String className, Class[] argTypes, Object[] args) throws Exception
    {
        return (V) buildBean(getClassInstance(className), argTypes, args);
    }
}
