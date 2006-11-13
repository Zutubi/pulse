package com.zutubi.pulse.spring;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.ObjectFactory;

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
