package com.zutubi.plugins.classloader;

import com.zutubi.plugins.PluginsTestCase;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;


public class PluginsClassLoaderTest extends PluginsTestCase
{
    public void testLoaderWithDirectory() throws Exception
    {
        File pluginsDirectory = getPluginsDirectory();

        // first make a classloader of the entire directory.
        ClassLoader loader = PluginsClassLoader.getInstance(pluginsDirectory.toURL());

        // check we got the right one back
        assertEquals(DirectoryClassLoader.class, loader.getClass());

        // we should get one resource back for each JAR in the directory (2)
        Enumeration resources = loader.getResources("pluginA.jar");
        assertTrue(resources.hasMoreElements());

        URL resource = (URL) resources.nextElement();
        assertTrue(resource.toExternalForm().endsWith("plugins/pluginA.jar"));
        assertFalse(resources.hasMoreElements());

        resources = loader.getResources("pluginB.jar");
        assertTrue(resources.hasMoreElements());

        resource = (URL) resources.nextElement();
        assertTrue(resource.toExternalForm().endsWith("plugins/pluginB.jar"));
        assertFalse(resources.hasMoreElements());
    }
}
