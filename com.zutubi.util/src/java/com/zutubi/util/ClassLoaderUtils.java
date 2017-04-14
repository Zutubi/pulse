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

package com.zutubi.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * ClassLoaderUtils a copy of an open source class.
 *
 */
public class ClassLoaderUtils
{
    public static <V> Class<V> loadAssociatedClass(Class annotatedClass, String className) throws ClassNotFoundException
    {
        if(!className.contains("."))
        {
            String prefix = annotatedClass.getName();
            int index = prefix.lastIndexOf('$');
            char separator = '$';

            if(index < 0)
            {
                index = prefix.lastIndexOf('.');
                separator = '.';
            }

            if(index >= 0)
            {
                prefix = prefix.substring(0, index);
            }

            className = prefix + separator + className;
        }

        return (Class<V>) annotatedClass.getClassLoader().loadClass(className);
    }

    /**
     * Load a class with a given name.
     * <p>
     * It will try to load the class in the following order:
     * <ul>
     *  <li>From {@link Thread#getContextClassLoader() Thread.currentThread().getContextClassLoader()}
     *  <li>Using the basic {@link Class#forName(java.lang.String) }
     *  <li>From {@link Class#getClassLoader() ClassLoaderUtil.class.getClassLoader()}
     *  <li>From the {@link Class#getClassLoader() callingClass.getClassLoader() }
     * </ul>
     *
     * @param className The name of the class to load
     * @param callingClass The Class object of the calling object
     * @throws ClassNotFoundException If the class cannot be found anywhere.
     */
    public static Class loadClass(String className, Class callingClass) throws ClassNotFoundException
    {
        try
        {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        }
        catch (ClassNotFoundException e)
        {
            try
            {
                return Class.forName(className);
            }
            catch (ClassNotFoundException ex)
            {
                try
                {
                    return ClassLoaderUtils.class.getClassLoader().loadClass(className);
                }
                catch (ClassNotFoundException exc)
                {
                    return callingClass.getClassLoader().loadClass(className);
                }

            }
        }
    }

    /**
     * Load a given resource.
     * <p>
     * This method will try to load the resource using the following methods (in order):
     * <ul>
     *  <li>From {@link Thread#getContextClassLoader() Thread.currentThread().getContextClassLoader()}
     *  <li>From {@link Class#getClassLoader() ClassLoaderUtil.class.getClassLoader()}
     *  <li>From the {@link Class#getClassLoader() callingClass.getClassLoader() }
     * </ul>
     *
     * @param resourceName The name of the resource to load
     * @param callingClass The Class object of the calling object
     */
    public static URL getResource(String resourceName, Class callingClass)
    {
        URL url = null;

        url = Thread.currentThread().getContextClassLoader().getResource(resourceName);

        if (url == null)
        {
            url = ClassLoaderUtils.class.getClassLoader().getResource(resourceName);
        }

        if (url == null)
        {
            url = callingClass.getClassLoader().getResource(resourceName);
        }
        return url;
    }

     /**
     * returns all found resources as java.net.URLs.
     * <p>
     * This method will try to load the resource using the following methods (in order):
     * <ul>
     *  <li>From {@link Thread#getContextClassLoader() Thread.currentThread().getContextClassLoader()}
     *  <li>From {@link Class#getClassLoader() ClassLoaderUtil.class.getClassLoader()}
     *  <li>From the {@link Class#getClassLoader() callingClass.getClassLoader() }
     * </ul>
     *
     * @param resourceName The name of the resource to load
     * @param callingClass The Class object of the calling object
     */
    public static Enumeration<URL> getResources(String resourceName, Class callingClass) throws IOException
    {
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(resourceName);
        if (urls == null)
        {
            urls = ClassLoaderUtils.class.getClassLoader().getResources(resourceName);
            if (urls == null)
            {
                urls = callingClass.getClassLoader().getResources(resourceName);
            }
        }

        return urls;
    }

    /**
     * This is a convenience method to load a resource as a stream.
     *
     * The algorithm used to find the resource is given in getResource()
     *
     * @param resourceName The name of the resource to load
     * @param callingClass The Class object of the calling object
     */
    public static InputStream getResourceAsStream(String resourceName, Class callingClass)
    {
        URL url = getResource(resourceName, callingClass);
        try
        {
            return url != null ? url.openStream() : null;
        }
        catch (IOException e)
        {
            return null;
        }
    }

    /**
     * Prints the current classloader hierarchy - useful for debugging.
     */
    public static void printClassLoader()
    {
        System.err.println("ClassLoaderUtils.printClassLoader");
        printClassLoader(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Prints the classloader hierarchy from a given classloader - useful for debugging.
     */
    public static void printClassLoader(ClassLoader cl)
    {
        System.err.println("ClassLoaderUtils.printClassLoader(cl = " + cl + ")");
        if (cl != null)
        {
            printClassLoader(cl.getParent());
        }
    }
}