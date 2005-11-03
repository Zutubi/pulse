package com.cinnamonbob.core;

import com.opensymphony.util.ClassLoaderUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation adapted from com.opensymphony.xwork.ObjectFactory
 */
public class ObjectFactory
{
    private static ObjectFactory instance = new ObjectFactory();

    private final Map<String, Class> classes = new HashMap<String, Class>();

    public static ObjectFactory getObjectFactory()
    {
        return instance;
    }

    public static void setObjectFactory(ObjectFactory factory)
    {
        instance = factory;
    }

    public Class getClassInstance(String className) throws ClassNotFoundException {

        Class clazz = classes.get(className);
        if (clazz == null) {
            clazz = ClassLoaderUtil.loadClass(className, this.getClass());
            classes.put(className, clazz);
        }

        return clazz;
    }

    public Object buildBean(Class clazz) throws Exception {
        return clazz.newInstance();
    }

    public <U> U buildBean(String className) throws Exception {
        Class clazz = getClassInstance(className);
        return (U)clazz.newInstance();
    }
}
