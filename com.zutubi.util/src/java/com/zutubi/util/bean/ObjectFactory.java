package com.zutubi.util.bean;

/**
 * Interface for a generic factory, used to build instances by type.
 */
public interface ObjectFactory
{
    public <T> T buildBean(Class<? extends T> clazz) throws Exception;

    public <T> T buildBean(String className, Class<? super T> token) throws Exception;

    <T> T buildBean(Class<? extends T> clazz, Class[] argTypes, Object[] args) throws Exception;

    <T> T buildBean(String className, Class<? super T> token, Class[] argTypes, Object[] args) throws Exception;

    <T> Class<? extends T> getClassInstance(String className, Class<? super T> token) throws ClassNotFoundException;
}
