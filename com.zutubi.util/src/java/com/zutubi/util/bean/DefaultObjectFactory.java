/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.util.bean;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.zutubi.util.reflection.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * A default implementation of an object factory.  Beans are created using
 * reflection to find and call constructors.
 * 
 * Implementation adapted from com.opensymphony.xwork.ObjectFactory
 */
public class DefaultObjectFactory implements ObjectFactory
{
    private  final Map<String, Class> classes = new HashMap<String, Class>();

    // not sure if this method belongs in this interface... it is pretty broken when considering
    // plugins.
    public <T> Class<? extends T> getClassInstance(String className, Class<? super T> supertype)
    {
        Class<?> clazz = classes.get(className);
        if (clazz == null)
        {
            try
            {
                clazz = getClass().getClassLoader().loadClass(className);
            }
            catch (ClassNotFoundException e)
            {
                throw new RuntimeException("Unable to find class '" + className + "': " + e.getMessage(), e);
            }

            if(!supertype.isAssignableFrom(clazz))
            {
                throw new ClassCastException("Loaded class '" + clazz.getName() + "' is not a subtype of '" + supertype.getName() + "'");
            }

            classes.put(className, clazz);
        }

        // We use the type token parameter to verify the classes we load.  Keep
        // the warning suppression as narrowly-scoped as possible, meaning we
        // also need to suppress IDEA's inspection.
        //noinspection UnnecessaryLocalVariable
        @SuppressWarnings("unchecked")
        Class<? extends T> result = (Class<? extends T>) clazz;
        return result;
    }

    /**
     * Create an instance of the bean defined by the specified class, using the
     * default constructor.
     *
     * @param clazz the type to create an instance of
     * @return a new instance of the given type
     * @throws RuntimeException on any error reflecting or building the object
     */
    public <T> T buildBean(Class<? extends T> clazz)
    {
        return newInstance(clazz);
    }

    public <T> T buildBean(String className, Class<? super T> supertype)
    {
        // javac cannot infer this type argument
        Class<? extends T> clazz = this.<T>getClassInstance(className, supertype);
        return newInstance(clazz);
    }

    public <T> T buildBean(Class<? extends T> clazz, final Object... args)
    {
        List<Constructor<?>> constructors = Arrays.asList(clazz.getConstructors());
        final Class<?>[] argTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++)
        {
            argTypes[i] = args[i] == null ? null : args[i].getClass();
        }

        Collection<Constructor<?>> matchingConstructors = Collections2.filter(constructors, new Predicate<Constructor<?>>()
        {
            public boolean apply(Constructor<?> constructor)
            {
                return ReflectionUtils.acceptsParameters(constructor, argTypes);
            }
        });

        if (matchingConstructors.size() != 1)
        {
            throw new RuntimeException("Could not instantiate object of type '" + clazz.getName() + "': " + matchingConstructors.size() + " matching constructors found");
        }

        try
        {
            return clazz.cast(matchingConstructors.iterator().next().newInstance(args));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to instantiate object of type '" + clazz.getName() + "': " + e.getMessage(), e);
        }
    }

    public <T> T buildBean(Class<? extends T> clazz, Class[] argTypes, Object[] args)
    {
        try
        {
            Constructor<? extends T> constructor = clazz.getConstructor(argTypes);
            return constructor.newInstance(args);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to instantiate object of type '" + clazz.getName() + "': " + e.getMessage(), e);
        }
    }

    public <T> T buildBean(String className, Class<? super T> supertype, Class[] argTypes, Object[] args)
    {
        // javac cannot infer this type argument
        Class<? extends T> clazz = this.<T>getClassInstance(className, supertype);
        return buildBean(clazz, argTypes, args);
    }

    private <T> T newInstance(Class<? extends T> clazz)
    {
        try
        {
            return clazz.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to instantiate object of type '" + clazz.getName() + "': " + e.getMessage(), e);
        }
    }
}
