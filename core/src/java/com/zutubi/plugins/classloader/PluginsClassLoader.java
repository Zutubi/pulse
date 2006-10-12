package com.zutubi.plugins.classloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Hani Suleiman (hani@formicary.net)
 *         (derived from WebWork 1's WebworkClassLoader)
 */
public abstract class PluginsClassLoader extends SecureClassLoader implements Cloneable
{
    protected String[] packages = null;
    
    private Map<String, Class> cache = new HashMap<String, Class>();

    protected PluginsClassLoader(ClassLoader parent)
    {
        super(parent);
    }

    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        Class c = (Class) cache.get(name);
        if (c != null)
        {
            return c;
        }

        boolean handles = false;
        if (packages != null)
        {
            for (String pkg : packages)
            {
                if (name.startsWith(pkg))
                {
                    handles = true;
                    break;
                }
            }
        }
        if (!handles)
        {
            return super.loadClass(name, resolve);
        }
        try
        {
            c = findClass(name);
            cache.put(name, c);
        }
        catch (ClassNotFoundException ex)
        {
            return super.loadClass(name, resolve);
        }
        return c;
    }

    protected Class findClass(String name) throws ClassNotFoundException
    {
        String path = name.replace('.', '/').concat(".class");
        byte[] data = getFile(path);
        if (data == null)
        {
            throw new ClassNotFoundException();
        }

        return defineClass(name, data, 0, data.length);
    }

    public URL findResource(String name)
    {
        byte[] data = this.getFile(name);

        if (data == null)
        {
            return null;
        }
        try
        {
            return getDataURL(name, data);
        }
        catch (MalformedURLException e)
        {
            return null;
        }
    }

    protected abstract URL getDataURL(String name, byte[] data) throws MalformedURLException;

    public Enumeration<URL> findResources(String name)
    {
        URL url = this.findResource(name);

        if (url == null)
            return null;

        return Collections.enumeration(Collections.singleton(url));
    }

    protected abstract byte[] getFile(String path);

    public static ClassLoader getInstance(URL url)
    {
        return getInstance(url, ClassLoader.getSystemClassLoader());
    }

    public static ClassLoader getInstance(URL url, ClassLoader parent)
    {
        ClassLoader loader;
        File file;

        try
        {
            file = new File(url.toURI());
        }
        catch(URISyntaxException e)
        {
            file = new File(url.toString());
        }
        
        if (file.isDirectory())
        {
            loader = new DirectoryClassLoader(file, parent);
        }
        else
        {
            loader = new JarClassLoader(file, parent);
        }
        return loader;
    }

    public abstract Object clone();

    /**
     * Method from WebWork1 webwork.util.ClassLoaderUtils
     */
    public static byte[] readStream(InputStream in, int size) throws IOException
    {
        if (in == null)
        {
            return null;
        }
        if (size == 0)
        {
            return new byte[0];
        }
        int currentTotal = 0;
        int bytesRead;
        byte[] data = new byte[size];
        while (currentTotal < data.length && (bytesRead = in.read(data, currentTotal, data.length - currentTotal)) >= 0)
        {
            currentTotal += bytesRead;
        }
        return data;
    }

    public URL getResource(String name)
    {
        URL url = findResource(name);
        if (url != null)
        {
            return url;
        }

        return super.getResource(name);
    }

    /**
     * Clean any resources held by the Classloader. ie, close files etc etc.
     */
    public void close()
    {
    }
}