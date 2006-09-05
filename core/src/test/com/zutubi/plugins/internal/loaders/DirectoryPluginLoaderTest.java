package com.zutubi.plugins.internal.loaders;

import com.zutubi.plugins.Plugin;
import com.zutubi.plugins.PluginsTestCase;

import java.io.File;
import java.util.Collection;

/**
 * <class-comment/>
 */
public class DirectoryPluginLoaderTest extends PluginsTestCase
{
    private DirectoryPluginLoader loader;

    public DirectoryPluginLoaderTest()
    {
    }

    public DirectoryPluginLoaderTest(String string)
    {
        super(string);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        loader = new DirectoryPluginLoader();
        this.loader.setComponentDescriptorFactory(descriptorFactory);
    }

    protected void tearDown() throws Exception
    {
        loader = null;

        super.tearDown();
    }

    public void testLoadPluginsFromDirectory()
    {
        File base = new File(getPluginsDirectory(), "pluginD");
        assertTrue(base.isDirectory());

        loader.setBaseDirectory(base);
        Collection<Plugin> plugins = loader.loadPlugins();
        assertNotNull(plugins);
        assertEquals(1, plugins.size());

        Plugin plugin = plugins.iterator().next();
        assertEquals("com.zutubi.plugins.sampleD", plugin.getKey());
    }
}
