package com.cinnamonbob.core;

import com.opensymphony.util.ClassLoaderUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation adapted from com.opensymphony.xwork.ObjectFactory
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

    public <V> V buildBean(Class clazz) throws Exception
    {
        return (V) clazz.newInstance();
    }

    public <U> U buildBean(String className) throws Exception
    {
        Class clazz = getClassInstance(className);
        return (U) clazz.newInstance();
    }
}
