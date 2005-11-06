package com.cinnamonbob.spring;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.core.ObjectFactory;

/**
 * <class-comment/>
 */
public class SpringObjectFactory extends ObjectFactory
{
    public Object buildBean(Class clazz) throws Exception {
        Object bean = super.buildBean(clazz);
        ComponentContext.autowire(bean);
        return bean;
    }

    public <V> V buildBean(String className) throws Exception {
        V bean = (V)super.buildBean(className);
        ComponentContext.autowire(bean);
        return bean;
    }
}
