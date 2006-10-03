package com.zutubi.plugins.repositories;

import com.zutubi.plugins.Plugin;
import com.zutubi.plugins.PluginsTestCase;
import com.zutubi.plugins.internal.DefaultComponentDescriptorFactory;
import com.zutubi.plugins.internal.loaders.ArchivePluginLoader;
import com.zutubi.plugins.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * <class-comment/>
 */
public class LocalPluginRepositoryTest extends PluginsTestCase
{
    private LocalPluginRepository repository;
    private File repositoryDirectory;

    public LocalPluginRepositoryTest()
    {
    }

    public LocalPluginRepositoryTest(String string)
    {
        super(string);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        // setup the temporary directory.
        repositoryDirectory = File.createTempFile("pluginRepository", "");
        if (!repositoryDirectory.delete() || !repositoryDirectory.mkdirs())
        {
            fail();
        }

        repository = new LocalPluginRepository(repositoryDirectory);
        repository.setDescriptorFactory(new DefaultComponentDescriptorFactory());

    }

    protected void tearDown() throws Exception
    {
        repository.destory();
        repository = null;

        if (!FileUtils.deleteDir(repositoryDirectory))
        {
            fail();
        }

        super.tearDown();
    }

    public void testGetPluginsFromEmptyDirectory()
    {
        List<Plugin> plugins = repository.getPlugins();
        assertNotNull(plugins);
        assertEquals(0, plugins.size());
    }

    public void testGetPluginsWhenSinglePluginJarInstalled() throws IOException
    {
        copyToDir(pluginA, repositoryDirectory);
        repository.scanForPlugins();

        List<Plugin> plugins = repository.getPlugins();
        assertNotNull(plugins);
        assertEquals(1, plugins.size());
    }

    public void testGetPluginsWhenSinglePluginZipInstalled() throws IOException
    {
        copyToDir(pluginC, repositoryDirectory);
        repository.scanForPlugins();

        List<Plugin> plugins = repository.getPlugins();
        assertNotNull(plugins);
        assertEquals(1, plugins.size());
    }

    public void testGetPluginsWhenMultiplePluginsInstalled() throws IOException
    {
        copyToDir(pluginA, repositoryDirectory);
        copyToDir(pluginC, repositoryDirectory);
        repository.scanForPlugins();

        List<Plugin> plugins = repository.getPlugins();
        assertNotNull(plugins);
        assertEquals(2, plugins.size());
    }

    public void testInstallJar()
    {
        repository.scanForPlugins();
        assertEquals(0, repositoryDirectory.list().length);
        Plugin p = loadPlugin(pluginA);
        repository.installPlugin(p);
        assertEquals(1, repositoryDirectory.list().length);
    }

    public void testInstallZip()
    {
        repository.scanForPlugins();
        assertEquals(0, repositoryDirectory.list().length);
        Plugin p = loadPlugin(pluginC);
        repository.installPlugin(p);
        assertEquals(1, repositoryDirectory.list().length);
    }

    public void testUninstallJar() throws IOException
    {
        File pluginJar = copyToDir(pluginA, repositoryDirectory);
        repository.scanForPlugins();

        Plugin p = repository.getPlugins().iterator().next();
        repository.uninstallPlugin(p);
        assertFalse(pluginJar.isFile());
    }

    public void testUninstallZip() throws IOException
    {
        File pluginZip = copyToDir(pluginC, repositoryDirectory);
        repository.scanForPlugins();

        Plugin p = repository.getPlugins().iterator().next();
        repository.uninstallPlugin(p);
        assertFalse(pluginZip.isFile());
    }

    public void testListPluginsFromEmptyDirectory()
    {
        List<String> pluginListing = repository.listPlugins();
        assertNotNull(pluginListing);
        assertEquals(0, pluginListing.size());
    }

    private Plugin loadPlugin(File f)
    {
        ArchivePluginLoader loader = new ArchivePluginLoader(f);
        loader.setComponentDescriptorFactory(descriptorFactory);
        return loader.loadPlugins().iterator().next();
    }
}
