package com.zutubi.plugins;

/**
 * <class-comment/>
 */
public interface ObjectFactory
{
    public <V> V buildBean(Class clazz) throws Exception;

    public <U> U buildBean(String className) throws Exception;    
}
