package com.zutubi.plugins.internal;

import com.zutubi.plugins.utils.ClassLoaderUtils;
import com.zutubi.plugins.ObjectFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * <class-comment/>
 */
public class DefaultObjectFactory implements ObjectFactory
{
    private final Map<String, Class> classes = new HashMap<String, Class>();

    public Class getClassInstance(String className) throws ClassNotFoundException
    {

        Class clazz = classes.get(className);
        if (clazz == null)
        {
            clazz = ClassLoaderUtils.loadClass(className, this.getClass());
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
