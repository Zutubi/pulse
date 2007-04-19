package com.zutubi.util.bean;

/**
 * <class-comment/>
 */
public interface ObjectFactory
{
    public <V> V buildBean(Class<V> clazz) throws Exception;

    public <U> U buildBean(String className) throws Exception;

    <W> W buildBean(Class<W> clazz, Class[] argTypes, Object[] args) throws Exception;

    <X> X buildBean(String className, Class[] argTypes, Object[] args) throws Exception;

    Class getClassInstance(String className) throws ClassNotFoundException;
}
