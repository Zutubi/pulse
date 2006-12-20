package com.zutubi.plugins.internal.loaders;

import com.zutubi.plugins.Plugin;
import com.zutubi.plugins.PluginLoader;
import com.zutubi.plugins.PluginsTestCase;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * <class-comment/>
 */
public class ArchivePluginLoaderTest extends PluginsTestCase
{
    private ArchivePluginLoader loader;

    public ArchivePluginLoaderTest()
    {
    }

    public ArchivePluginLoaderTest(String string)
    {
        super(string);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        loader = new ArchivePluginLoader();
        loader.setComponentDescriptorFactory(descriptorFactory);
    }

    protected void tearDown() throws Exception
    {
        loader = null;

        super.tearDown();
    }

    public void testLoadPluginFromJar()
    {
        loader.setArchive(pluginA);
        Plugin plugin = loadPlugin(loader);
        assertNotNull(plugin);
        assertEquals("com.zutubi.plugins.sampleA", plugin.getKey());
    }

    public void testLoadPluginFromZip()
    {
        loader.setArchive(pluginC);
        Plugin plugin = loadPlugin(loader);
        assertNotNull(plugin);
        assertEquals("com.zutubi.plugins.sampleC", plugin.getKey());
    }

    public void testPluginSource() throws IOException, URISyntaxException
    {
        loader.setArchive(pluginC);
        Plugin plugin = loader.loadPlugins().iterator().next();
        File pluginSource = new File(plugin.getSource().toURI());

        assertEquals(pluginC.getCanonicalFile(), pluginSource.getCanonicalFile());
    }

    public void testFileValidation()
    {
        try
        {
            loader.setArchive(new File("not a file"));
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // noop
        }
        try
        {
            loader.setArchive(getPluginsDirectory());
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // noop
        }
    }

    public void testArchiveThatDoesNotContainAPlugin()
    {
        loader.setArchive(pluginX);
        Plugin plugin = loadPlugin(loader);
        assertNull(plugin);
    }

    public void testLoadPluginUsingAlternateDescriptor()
    {
        loader.setDescriptor("alternate-plugin.xml");
        loader.setArchive(pluginY);
        Plugin plugin = loadPlugin(loader);
        assertEquals("com.zutubi.plugins.sampleY", plugin.getKey());
    }

    private Plugin loadPlugin(PluginLoader loader)
    {
        Collection<Plugin> plugins = loader.loadPlugins();
        if (plugins.size() > 0)
        {
            return plugins.iterator().next();
        }
        return null;
    }

}
