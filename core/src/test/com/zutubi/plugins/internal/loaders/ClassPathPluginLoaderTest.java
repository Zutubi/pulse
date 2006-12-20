package com.zutubi.plugins.internal.loaders;

import com.zutubi.plugins.PluginsTestCase;
import com.zutubi.plugins.Plugin;

import java.util.List;

/**
 * <class-comment/>
 */
public class ClassPathPluginLoaderTest extends PluginsTestCase
{
    private ClassPathPluginLoader loader = null;

    public ClassPathPluginLoaderTest()
    {
    }

    public ClassPathPluginLoaderTest(String string)
    {
        super(string);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        this.loader = new ClassPathPluginLoader();
        this.loader.setComponentDescriptorFactory(descriptorFactory);
    }

    protected void tearDown() throws Exception
    {
        this.loader = null;

        super.tearDown();
    }

    public void testLoadDefaultPlugin()
    {
        List<Plugin> plugins = loader.loadPlugins();
        assertNotNull(plugins);
        assertEquals(1, plugins.size());

        Plugin p = plugins.get(0);
        assertEquals("com.zutubi.plugins.sample1", p.getKey());
    }

    public void testLoadAlternatePlugin()
    {
        loader.setDescriptor("com/zutubi/plugins/internal/loaders/sample-plugin.xml");
        List<Plugin> plugins = loader.loadPlugins();
        assertNotNull(plugins);
        assertEquals(1, plugins.size());

        Plugin p = plugins.get(0);
        assertEquals("com.zutubi.plugins.sample.SamplePlugin", p.getKey());
    }
}
