package com.zutubi.util.bean;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation adapted from com.opensymphony.xwork.ObjectFactory
 *
 */
public class DefaultObjectFactory implements ObjectFactory
{
    private final Map<String, Class> classes = new HashMap<String, Class>();

    // not sure if this method belongs in this interface... it is pretty broken when considering
    // plugins.
    public Class getClassInstance(String className) throws ClassNotFoundException
    {
        Class clazz = classes.get(className);
        if (clazz == null)
        {
            clazz = getClass().getClassLoader().loadClass(className);
            classes.put(className, clazz);
        }
        return clazz;
    }

    /**
     * Create an instance of the bean defined by the specified class.
     *
     * @param clazz
     *
     * @return
     *
     * @throws Exception
     */
    public <V> V buildBean(Class<V> clazz) throws Exception
    {
        return clazz.newInstance();
    }

    public <U> U buildBean(String className) throws Exception
    {
        Class<U> clazz = getClassInstance(className);
        return clazz.newInstance();
    }

    public <W> W buildBean(Class<W> clazz, Class[] argTypes, Object[] args) throws Exception
    {
        Constructor<W> constructor = clazz.getConstructor(argTypes);
        if (constructor != null)
        {
            return (W) constructor.newInstance(args);
        }
        throw new RuntimeException(String.format("Failed to locate the requested constructor for '%s'", clazz.getName()));
    }

    public <X> X buildBean(String className, Class[] argTypes, Object[] args) throws Exception
    {
        Class<X> clazz = getClassInstance(className);
        Constructor<X> constructor = clazz.getConstructor(argTypes);
        if (constructor != null)
        {
            return (X) constructor.newInstance(args);
        }
        throw new RuntimeException(String.format("Failed to locate the requested constructor for '%s'", clazz.getName()));
    }
}
