package com.zutubi.plugins.classloader;

import com.zutubi.plugins.PluginsTestCase;

import java.io.File;
import java.net.URL;
import java.util.Comparator;
import java.util.Enumeration;

public class JarClassLoaderTest extends PluginsTestCase
{
    /**
     *
     */
    public void testJarLoaderCanLoadClass() throws Exception
    {
        File pluginsDirectory = getPluginsDirectory();
        File pluginJar = new File(pluginsDirectory, "pluginA.jar");

        // make the JAR loader
        JarClassLoader loader = new JarClassLoader(pluginJar, this.getClass().getClassLoader());

        // now try to load the plugin class and cast it to something in the global class path
        Class classA = loader.findClass("com.zutubi.plugins.sample.A");
        Object a = classA.newInstance();

        // check we really did get the right class
        assertEquals("com.zutubi.plugins.sample.A", a.getClass().getName());
    }

    public void testJarLoaderCanLoadLinkedClassesFromJar() throws Exception
    {
        File pluginsDirectory = getPluginsDirectory();
        File pluginJar = new File(pluginsDirectory, "pluginB.jar");

        // make the JAR loader
        JarClassLoader loader = new JarClassLoader(pluginJar, this.getClass().getClassLoader());

        // Class C uses class B internally. This test ensures that B can be loaded by Cs class loader.
        Class classC = loader.findClass("com.zutubi.plugins.sample.C");
        Object c = classC.newInstance();

        assertEquals("com.zutubi.plugins.sample.C", c.getClass().getName());
    }

    public void testJarLoaderCanLoadClassAndCastToTypeDefinedOutsideJar() throws Exception
    {
        File pluginsDirectory = getPluginsDirectory();
        File pluginJar = new File(pluginsDirectory, "pluginB.jar");

        // make the JAR loader
        JarClassLoader loader = new JarClassLoader(pluginJar, this.getClass().getClassLoader());

        // Class C uses class B internally. This test ensures that B can be loaded by Cs class loader.
        Class classD = loader.findClass("com.zutubi.plugins.sample.D");
        Comparator d = (Comparator) classD.newInstance();

        assertEquals("com.zutubi.plugins.sample.D", d.getClass().getName());
    }

    public void testJarLoaderCanLoadResources() throws Exception
    {
        File pluginsDirectory = getPluginsDirectory();
        File pluginJar = new File(pluginsDirectory, "pluginB.jar");

        // make the JAR loader
        JarClassLoader loader = new JarClassLoader(pluginJar, this.getClass().getClassLoader());

        // check that we can load the plugin.xml resource from the jar root.
        Enumeration descriptors = loader.findResources("plugin.xml");
        assertTrue(descriptors.hasMoreElements());

        URL descriptor = (URL) descriptors.nextElement();
        assertTrue(descriptor.toExternalForm().endsWith("pluginB.jar!plugin.xml"));
        assertFalse(descriptors.hasMoreElements());

        // check that we can load the a.properties resource from deep down in the jar.
        descriptors = loader.findResources("com/zutubi/plugins/sample/resource/a.properties");

        assertTrue(descriptors.hasMoreElements());

        descriptor = (URL) descriptors.nextElement();
        assertTrue(descriptor.toExternalForm().endsWith("pluginB.jar!com/zutubi/plugins/sample/resource/a.properties"));
        assertFalse(descriptors.hasMoreElements());
    }

/*
    public void testJarLoaderDoesNotLoadExternalResources() throws Exception
    {
        File pluginsDirectory = getPluginsDirectory();
        File pluginJar = new File(pluginsDirectory, "pluginB.jar");

        JarClassLoader loader = new JarClassLoader(pluginJar, this.getClass().getClassLoader());
        loader.getResource("plugin.xml");
    }
*/
}
