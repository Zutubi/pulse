package com.zutubi.pulse.core;

import com.opensymphony.util.ClassLoaderUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.lang.reflect.Constructor;

/**
 * Implementation adapted from com.opensymphony.xwork.ObjectFactory
 *
 */
public class ObjectFactory
{
    private final Map<String, Class> classes = new HashMap<String, Class>();

    public Class getClassInstance(String className) throws ClassNotFoundException
    {
        Class clazz = classes.get(className);
        if (clazz == null)
        {
            clazz = ClassLoaderUtil.loadClass(className, this.getClass());
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
    public <V> V buildBean(Class clazz) throws Exception
    {
        return (V) clazz.newInstance();
    }

    public <U> U buildBean(String className) throws Exception
    {
        Class clazz = getClassInstance(className);
        return (U) clazz.newInstance();
    }

    public <W> W buildBean(Class clazz, Class[] argTypes, Object[] args) throws Exception
    {
        Constructor constructor = clazz.getConstructor(argTypes);
        if (constructor != null)
        {
            return (W) constructor.newInstance(args);
        }
        throw new RuntimeException(String.format("Failed to locate the requested constructor for '%s'", clazz.getName()));
    }

    public <X> X buildBean(String className, Class[] argTypes, Object[] args) throws Exception
    {
        Class clazz = getClassInstance(className);
        Constructor constructor = clazz.getConstructor(argTypes);
        if (constructor != null)
        {
            return (X) constructor.newInstance(args);
        }
        throw new RuntimeException(String.format("Failed to locate the requested constructor for '%s'", clazz.getName()));
    }

    private Class[] toClassArray(Object... objArray)
    {
        List<Class> classes = new LinkedList<Class>();
        for (Object obj : objArray)
        {
            if (obj != null)
            {
                classes.add(obj.getClass());
            }
            else
            {
                classes.add(null);
            }
        }
        return classes.toArray(new Class[classes.size()]);
    }
}
