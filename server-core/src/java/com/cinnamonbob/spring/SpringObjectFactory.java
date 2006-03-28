package com.cinnamonbob.spring;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.core.ObjectFactory;

/**
 * <class-comment/>
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
}
