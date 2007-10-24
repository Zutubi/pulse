package com.zutubi.pulse.plugins;

import com.zutubi.pulse.events.DataDirectoryLocatedEvent;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import org.osgi.framework.Bundle;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 */
public class DefaultPluginManagerTest extends PulseTestCase
{
    private static final String PRODUCER_ID = "com.zutubi.bundles.producer";
    private static final String CONSUMER_ID = "com.zutubi.bundles.consumer";

    private File tempDir;
    private DefaultPluginManager pluginManager;
    private URL producer1URL;
    private URL producer11URL;
    private URL producer2URL;
    private URL consumer1URL;
    private File bundleDir;

    protected void setUp() throws Exception
    {
        tempDir = FileSystemUtils.createTempDir(DefaultPluginManagerTest.class.getName(), "");
        File internalDir = new File(tempDir, "internal");
        internalDir.mkdir();
        File prepackagedDir = new File(tempDir, "prepackaged");
        prepackagedDir.mkdir();
        File userDir = new File(tempDir, "user");
        userDir.mkdir();

        ConfigurablePluginPaths paths = new ConfigurablePluginPaths();
        paths.setPluginConfigurationRootString(new File(getPulseRoot(), "master/etc/osgi").getAbsolutePath());
        paths.setInternalPluginRootString(internalDir.getAbsolutePath());
        paths.setPrepackagedPluginRootString(prepackagedDir.getAbsolutePath());
        paths.setUserPluginRootString(userDir.getAbsolutePath());

        pluginManager = new DefaultPluginManager();
        pluginManager.setPluginPaths(paths);

        bundleDir = getTestDataDir("core", "test-bundles");
        producer1URL = new File(bundleDir, "com.zutubi.bundles.producer_1.0.0.jar").toURL();
        producer11URL = new File(bundleDir, "com.zutubi.bundles.producer_1.1.0.jar").toURL();
        producer2URL = new File(bundleDir, "com.zutubi.bundles.producer_2.0.0.jar").toURL();
        consumer1URL = new File(bundleDir, "com.zutubi.bundles.consumer_1.0.0.jar").toURL();
    }

    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(tempDir);
    }

    public void testInstall() throws PluginException
    {
        init();
        try
        {
            assertEquals(0, pluginManager.getAllPlugins().size());
            Plugin plugin = installProducer1();
            assertEquals(Plugin.State.ENABLED, plugin.getState());
            List<? extends Plugin> plugins = pluginManager.getAllPlugins();
            assertEquals(1, plugins.size());
            assertEquals(plugin, plugins.get(0));
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testInstallDependent() throws Exception
    {
        init();
        try
        {
            assertEquals(0, pluginManager.getAllPlugins().size());
            installProducer1();
            Plugin plugin = installConsumer1();
            assertEquals(Plugin.State.ENABLED, plugin.getState());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testInstallDependencyMissing() throws Exception
    {
        init();
        try
        {
            assertEquals(0, pluginManager.getAllPlugins().size());
            Plugin plugin = installConsumer1();
            assertEquals(Plugin.State.DISABLED, plugin.getState());
            assertEquals("Unable to start plugin: The bundle could not be resolved. Reason: Missing Constraint: Require-Bundle: com.zutubi.bundles.producer; bundle-version=\"[1.0.0,2.0.0)\" (see logs for trace)", plugin.getErrorMessage());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testInstallSameId() throws Exception
    {
        init();
        try
        {
            assertEquals(0, pluginManager.getAllPlugins().size());
            installProducer1();
            installProducer11();
            fail();
        }
        catch(PluginException e)
        {
            assertEquals("Unable to load plugin: A plugin with the same identifier (com.zutubi.bundles.producer) already exists.", e.getMessage());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testInstallBadURL() throws Exception
    {
        init();
        try
        {
            assertEquals(0, pluginManager.getAllPlugins().size());
            pluginManager.installPlugin(new URL("file:///nosuchfile"));
            fail();
        }
        catch(PluginException e)
        {
            assertTrue(e.getMessage().contains("I/O error copying plugin"));
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testInstallBadManifest() throws Exception
    {
        init();
        try
        {
            pluginManager.installPlugin(new File(bundleDir, "empty.jar").toURL());
            fail();
        }
        catch(PluginException e)
        {
            assertEquals("Unable to load plugin: Invalid plugin: no symbolic name", e.getMessage());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testInstallBadJarFile() throws Exception
    {
        init();
        try
        {
            pluginManager.installPlugin(new File(bundleDir, "bad.jar").toURL());
            fail();
        }
        catch(PluginException e)
        {
            assertEquals("Unable to load plugin: java.util.zip.ZipException: error in opening zip file", e.getMessage());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testUninstall() throws Exception
    {
        init();
        try
        {
            PluginImpl plugin = installProducer1();
            assertEquals(Plugin.State.ENABLED, plugin.getState());
            assertTrue(plugin.getPluginFile().exists());
            pluginManager.uninstallPlugin(plugin);
            assertEquals(Plugin.State.UNINSTALLING, plugin.getState());
            
            restart();
            
            assertFalse(plugin.getPluginFile().exists());
            List<? extends Plugin> plugins = pluginManager.getAllPlugins();
            assertEquals(0, plugins.size());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testUninstallDependency() throws Exception
    {
        // We allow the plugin to be uninstalled at this level, but on
        // restart the dependent will fail to load.
        init();
        try
        {
            assertEquals(0, pluginManager.getAllPlugins().size());
            Plugin plugin = installProducer1();
            installConsumer1();
            assertEquals(Plugin.State.ENABLED, plugin.getState());
            pluginManager.uninstallPlugin(plugin);

            restart();

            List<? extends Plugin> allPlugins = pluginManager.getAllPlugins();
            assertEquals(1, allPlugins.size());
            Plugin consumerPlugin = allPlugins.get(0);
            assertEquals(CONSUMER_ID, consumerPlugin.getId());
            assertEquals(Plugin.State.DISABLED, consumerPlugin.getState());
            assertEquals("Unable to start plugin: The bundle could not be resolved. Reason: Missing Constraint: Require-Bundle: com.zutubi.bundles.producer; bundle-version=\"[1.0.0,2.0.0)\" (see logs for trace)", consumerPlugin.getErrorMessage());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testUninstallDisabled() throws Exception
    {
        init();
        try
        {
            PluginImpl plugin = installAndDisableProducer1();

            pluginManager.uninstallPlugin(plugin);

            assertEquals(Plugin.State.UNINSTALLING, plugin.getState());

            restart();

            assertFalse(plugin.getPluginFile().exists());
            List<? extends Plugin> plugins = pluginManager.getAllPlugins();
            assertEquals(0, plugins.size());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testUninstallDisabling() throws Exception
    {
        init();
        try
        {
            PluginImpl plugin = installProducer1();
            assertEquals(Plugin.State.ENABLED, plugin.getState());
            pluginManager.disablePlugin(plugin);
            pluginManager.uninstallPlugin(plugin);

            assertEquals(Plugin.State.UNINSTALLING, plugin.getState());

            restart();

            assertFalse(plugin.getPluginFile().exists());
            List<? extends Plugin> plugins = pluginManager.getAllPlugins();
            assertEquals(0, plugins.size());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testUninstallUpdating() throws Exception
    {
        init();
        try
        {
            PluginImpl plugin = installAndMarkForUpdateProducer1();

            pluginManager.uninstallPlugin(plugin);
            fail();
        }
        catch(PluginException e)
        {
            assertEquals("Cannot uninstall plugin: already marked for update", e.getMessage());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testUninstallUninstalling() throws Exception
    {
        init();
        try
        {
            PluginImpl plugin = installProducer1();
            assertEquals(Plugin.State.ENABLED, plugin.getState());
            assertTrue(plugin.getPluginFile().exists());
            pluginManager.uninstallPlugin(plugin);
            pluginManager.uninstallPlugin(plugin);
            fail();
        }
        catch(PluginException e)
        {
            assertEquals("Cannot uninstall plugin: already marked for uninstall", e.getMessage());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testUpdate() throws PluginException
    {
        init();
        try
        {
            PluginImpl oldPlugin = installAndMarkForUpdateProducer1();
            File oldFile = oldPlugin.getPluginFile();

            restart();

            List<? extends Plugin> all = pluginManager.getAllPlugins();
            assertEquals(1, all.size());
            assertProducer11(all.get(0));
            assertFalse(oldFile.exists());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testUpdateDependencyOK() throws PluginException
    {
        init();
        try
        {
            PluginImpl oldPlugin = installProducer1();
            File oldFile = oldPlugin.getPluginFile();
            installConsumer1();

            pluginManager.updatePlugin(oldPlugin, producer11URL);
            assertEquals(Plugin.State.UPDATING, oldPlugin.getState());
            List<? extends Plugin> all = pluginManager.getAllPlugins();
            assertEquals(2, all.size());

            restart();

            all = pluginManager.getAllPlugins();
            assertFalse(oldFile.exists());

            assertEquals(2, all.size());
            PluginImpl producer = pluginManager.getPlugin(PRODUCER_ID);
            assertNotNull(producer);
            assertEquals("1.1.0", producer.getVersion());

            PluginImpl consumer = pluginManager.getPlugin(CONSUMER_ID);
            assertNotNull(consumer);
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testUpdateDependencyBroken() throws PluginException
    {
        init();
        try
        {
            PluginImpl oldPlugin = installProducer1();
            File oldFile = oldPlugin.getPluginFile();
            installConsumer1();
            pluginManager.updatePlugin(oldPlugin, producer2URL);
            assertEquals(Plugin.State.UPDATING, oldPlugin.getState());
            List<? extends Plugin> all = pluginManager.getAllPlugins();
            assertEquals(2, all.size());

            restart();

            all = pluginManager.getAllPlugins();
            assertFalse(oldFile.exists());

            assertEquals(2, all.size());
            PluginImpl producer = pluginManager.getPlugin(PRODUCER_ID);
            assertProducer2(producer);
            assertEquals(Plugin.State.ENABLED, producer.getState());

            PluginImpl consumer = pluginManager.getPlugin(CONSUMER_ID);
            assertNotNull(consumer);
            assertEquals(Plugin.State.DISABLED, consumer.getState());
            assertEquals("Unable to start plugin: The bundle could not be resolved. Reason: Missing Constraint: Require-Bundle: com.zutubi.bundles.producer; bundle-version=\"[1.0.0,2.0.0)\" (see logs for trace)", consumer.getErrorMessage());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testUpdateDisabled() throws PluginException
    {
        init();
        try
        {
            PluginImpl oldPlugin = installAndDisableProducer1();
            File oldFile = oldPlugin.getPluginFile();
            pluginManager.updatePlugin(oldPlugin, producer11URL);

            assertEquals(Plugin.State.UPDATING, oldPlugin.getState());
            List<? extends Plugin> all = pluginManager.getAllPlugins();
            assertEquals(1, all.size());
            assertEquals(oldPlugin, all.get(0));

            restart();

            all = pluginManager.getAllPlugins();
            assertEquals(1, all.size());
            Plugin newPlugin = all.get(0);
            assertProducer11(newPlugin);
            assertEquals(Plugin.State.DISABLED, newPlugin.getState());
            assertFalse(oldFile.exists());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testUpdateDisabling() throws PluginException
    {
        init();
        try
        {
            PluginImpl oldPlugin = installProducer1();
            File oldFile = oldPlugin.getPluginFile();
            pluginManager.disablePlugin(oldPlugin);
            assertEquals(Plugin.State.DISABLING, oldPlugin.getState());

            pluginManager.updatePlugin(oldPlugin, producer11URL);
            assertEquals(Plugin.State.UPDATING, oldPlugin.getState());
            List<? extends Plugin> all = pluginManager.getAllPlugins();
            assertEquals(1, all.size());
            assertEquals(oldPlugin, all.get(0));

            restart();

            all = pluginManager.getAllPlugins();
            assertEquals(1, all.size());
            Plugin newPlugin = all.get(0);
            assertProducer11(newPlugin);
            assertEquals(Plugin.State.DISABLED, newPlugin.getState());
            assertFalse(oldFile.exists());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testUpdateUninstalling() throws PluginException
    {
        init();
        try
        {
            PluginImpl oldPlugin = installAndMarkForUninstallProducer1();
            pluginManager.updatePlugin(oldPlugin, producer11URL);
            fail();
        }
        catch(PluginException e)
        {
            assertEquals("Unable to update plugin: already marked for uninstall", e.getMessage());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testUpdateUpdating() throws PluginException
    {
        init();
        try
        {
            PluginImpl oldPlugin = installAndMarkForUpdateProducer1();
            pluginManager.updatePlugin(oldPlugin, producer2URL);
            fail();
        }
        catch(PluginException e)
        {
            assertEquals("Unable to update plugin: already marked for update", e.getMessage());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testDisable() throws PluginException
    {
        init();
        try
        {
            PluginImpl plugin = installProducer1();
            pluginManager.disablePlugin(plugin);
            assertEquals(Plugin.State.DISABLING, plugin.getState());
            assertEquals(1, pluginManager.getAllPlugins().size());

            restart();
            assertDisabledProducer1();
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testDisableDependency() throws Exception
    {
        // We allow the plugin to be uninstalled at this level, but on
        // restart the dependent will fail to load.
        init();
        try
        {
            Plugin plugin = installProducer1();
            installConsumer1();
            assertEquals(Plugin.State.ENABLED, plugin.getState());
            pluginManager.disablePlugin(plugin);

            restart();

            assertDisabled(pluginManager.getPlugin(PRODUCER_ID));
            Plugin consumerPlugin = pluginManager.getPlugin(CONSUMER_ID);
            assertEquals(Plugin.State.DISABLED, consumerPlugin.getState());
            assertEquals("Unable to start plugin: The bundle could not be resolved. Reason: Missing Constraint: Require-Bundle: com.zutubi.bundles.producer; bundle-version=\"[1.0.0,2.0.0)\" (see logs for trace)", consumerPlugin.getErrorMessage());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testDisableDisabling() throws PluginException
    {
        init();
        try
        {
            PluginImpl plugin = installProducer1();
            pluginManager.disablePlugin(plugin);
            assertEquals(Plugin.State.DISABLING, plugin.getState());
            pluginManager.disablePlugin(plugin);
            assertEquals(Plugin.State.DISABLING, plugin.getState());
            assertEquals(1, pluginManager.getAllPlugins().size());

            restart();
            assertDisabledProducer1();
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testDisableDisabled() throws PluginException
    {
        init();
        try
        {
            PluginImpl plugin = installProducer1();
            pluginManager.disablePlugin(plugin);

            restart();

            plugin = assertDisabledProducer1();
            pluginManager.disablePlugin(plugin);
            assertDisabledProducer1();
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testDisableUninstalling() throws PluginException
    {
        init();
        try
        {
            PluginImpl plugin = installAndMarkForUninstallProducer1();
            pluginManager.disablePlugin(plugin);
            fail();
        }
        catch(PluginException e)
        {
            assertEquals("Unable to disable plugin: already marked for uninstall", e.getMessage());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testDisableUpdating() throws PluginException
    {
        init();
        try
        {
            PluginImpl plugin = installAndMarkForUpdateProducer1();
            pluginManager.disablePlugin(plugin);
            fail();
        }
        catch(PluginException e)
        {
            assertEquals("Unable to disable plugin: already marked for update", e.getMessage());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testEnable() throws PluginException
    {
        init();
        try
        {
            PluginImpl plugin = installProducer1();
            pluginManager.disablePlugin(plugin);

            restart();
            
            plugin = assertDisabledProducer1();
            pluginManager.enablePlugin(plugin);
            assertEnabledProducer1();
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testEnableEnabled() throws PluginException
    {
        init();
        try
        {
            PluginImpl plugin = installProducer1();
            assertEnabledProducer1();
            pluginManager.enablePlugin(plugin);
            assertEnabledProducer1();
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testEnableDisabled() throws PluginException
    {
        init();
        try
        {
            PluginImpl plugin = installProducer1();
            pluginManager.disablePlugin(plugin);

            restart();
            
            plugin = assertDisabledProducer1();
            pluginManager.enablePlugin(plugin);
            assertEnabledProducer1();

            restart();

            assertEnabledProducer1();
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testEnableDisabling() throws PluginException
    {
        init();
        try
        {
            PluginImpl plugin = installProducer1();
            pluginManager.disablePlugin(plugin);
            pluginManager.enablePlugin(plugin);
            assertEnabledProducer1();

            restart();

            assertEnabledProducer1();
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testEnableUninstalling() throws PluginException
    {
        init();
        try
        {
            PluginImpl plugin = installAndMarkForUninstallProducer1();
            pluginManager.enablePlugin(plugin);
            fail();
        }
        catch(PluginException e)
        {
            assertEquals("Unable to enable plugin: already marked for uninstall", e.getMessage());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testEnableUpdating() throws PluginException
    {
        init();
        try
        {
            PluginImpl plugin = installAndMarkForUpdateProducer1();
            pluginManager.enablePlugin(plugin);
            fail();
        }
        catch(PluginException e)
        {
            assertEquals("Unable to enable plugin: already marked for update", e.getMessage());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testDiscovery() throws PluginException
    {
        pluginManager.copyInPlugin(pluginManager.deriveName(producer1URL), producer11URL);
        init();
        try
        {
            assertEnabledProducer1();
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testUndiscovery() throws PluginException
    {
        File f = pluginManager.copyInPlugin(pluginManager.deriveName(producer1URL), producer11URL);
        init();
        try
        {
            assertEnabledProducer1();
            pluginManager.destroy();
            f.delete();
            assertFalse(f.isFile());
            init();
            assertEquals(0, pluginManager.getAllPlugins().size());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testGetDependentPlugins() throws PluginException
    {
        init();
        try
        {
            Plugin plugin = installProducer1();
            installConsumer1();
            List<Plugin> deps = pluginManager.getDependentPlugins(plugin);
            assertEquals(1, deps.size());
            assertEquals(CONSUMER_ID, deps.get(0).getId());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    public void testGetRequiredPlugins() throws PluginException
    {
        init();
        try
        {
            Plugin producer = installProducer1();
            Plugin consumer = installConsumer1();
            List<PluginRequirement> reqs = pluginManager.getRequiredPlugins(consumer);
            assertEquals(1, reqs.size());
            assertEquals(PRODUCER_ID, reqs.get(0).getId());
            assertEquals("[1.0.0, 2.0.0)", reqs.get(0).getVersionRange().toString());
            assertEquals(producer, reqs.get(0).getSupplier());
        }
        finally
        {
            pluginManager.destroy();
        }
    }

    private PluginImpl assertDisabledProducer1()
    {
        PluginImpl plugin;
        List<? extends Plugin> all = pluginManager.getAllPlugins();
        assertEquals(1, all.size());
        plugin = (PluginImpl) all.get(0);
        assertEquals(PRODUCER_ID, plugin.getId());
        assertDisabled(plugin);
        return plugin;
    }

    private void assertDisabled(PluginImpl plugin)
    {
        assertEquals(Plugin.State.DISABLED, plugin.getState());
        assertNull(plugin.getBundle());
        assertNull(plugin.getErrorMessage());
    }

    private PluginImpl assertEnabledProducer1()
    {
        PluginImpl plugin;
        List<? extends Plugin> all = pluginManager.getAllPlugins();
        assertEquals(1, all.size());
        plugin = (PluginImpl) all.get(0);
        assertEquals(PRODUCER_ID, plugin.getId());
        assertEnabled(plugin);
        return plugin;
    }

    private void assertEnabled(PluginImpl plugin)
    {
        assertEquals(Plugin.State.ENABLED, plugin.getState());
        Bundle bundle = plugin.getBundle();
        assertNotNull(bundle);
        assertEquals(Bundle.ACTIVE, bundle.getState());
        assertNull(plugin.getErrorMessage());
    }

    private void init()
    {
        pluginManager.init();
        pluginManager.handleEvent(new DataDirectoryLocatedEvent(this));
    }

    private void restart()
    {
        pluginManager.destroy();
        init();
    }

    private PluginImpl installProducer1() throws PluginException
    {
        Plugin plugin = pluginManager.installPlugin(producer1URL);
        assertEquals(PRODUCER_ID, plugin.getId());
        assertEquals("1.0.0", plugin.getVersion());
        return (PluginImpl) plugin;
    }

    private PluginImpl installAndDisableProducer1() throws PluginException
    {
        PluginImpl plugin = installProducer1();
        assertEquals(Plugin.State.ENABLED, plugin.getState());
        pluginManager.disablePlugin(plugin);

        restart();
        assertEquals(Plugin.State.DISABLED, pluginManager.getPlugin(PRODUCER_ID).getState());
        return pluginManager.getPlugin(PRODUCER_ID);
    }

    private PluginImpl installAndMarkForUninstallProducer1() throws PluginException
    {
        PluginImpl oldPlugin = installProducer1();
        pluginManager.uninstallPlugin(oldPlugin);
        assertEquals(Plugin.State.UNINSTALLING, oldPlugin.getState());
        return oldPlugin;
    }

    private PluginImpl installAndMarkForUpdateProducer1() throws PluginException
    {
        PluginImpl oldPlugin = installProducer1();
        pluginManager.updatePlugin(oldPlugin, producer11URL);
        assertEquals(Plugin.State.UPDATING, oldPlugin.getState());
        List<? extends Plugin> all = pluginManager.getAllPlugins();
        assertEquals(1, all.size());
        assertEquals(oldPlugin, all.get(0));
        return oldPlugin;
    }

    private Plugin installProducer11() throws PluginException
    {
        Plugin plugin = pluginManager.installPlugin(producer11URL);
        assertProducer11(plugin);
        return plugin;
    }

    private void assertProducer11(Plugin plugin)
    {
        assertEquals(PRODUCER_ID, plugin.getId());
        assertEquals("1.1.0", plugin.getVersion());
    }

    private void assertProducer2(PluginImpl producer)
    {
        assertNotNull(producer);
        assertEquals(PRODUCER_ID, producer.getId());
        assertEquals("2.0.0", producer.getVersion());
    }

    private Plugin installConsumer1() throws PluginException
    {
        Plugin plugin = pluginManager.installPlugin(consumer1URL);
        assertEquals(CONSUMER_ID, plugin.getId());
        assertEquals("1.0.0", plugin.getVersion());
        return plugin;
    }
}
