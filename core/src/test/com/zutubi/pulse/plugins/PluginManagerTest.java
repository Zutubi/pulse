package com.zutubi.pulse.plugins;

import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 *
 */
public class PluginManagerTest extends BasePluginSystemTestCase
{
    private static final String PRODUCER_ID = "com.zutubi.bundles.producer";
    private static final String CONSUMER_ID = "com.zutubi.bundles.consumer";

    private File producer1;
    private File producer11;
    private File producer2;
    private File producer3;
    private File consumer1;
    private File bad;
    private File failonstartup;

    protected void setUp() throws Exception
    {
        super.setUp();

        File bundleDir = getTestDataDir("core", "test-bundles");
        producer1 = new File(bundleDir, "com.zutubi.bundles.producer_1.0.0.jar");
        producer11 = new File(bundleDir, "com.zutubi.bundles.producer_1.1.0.jar");
        producer2 = new File(bundleDir, "com.zutubi.bundles.producer_2.0.0.jar");
        producer3 = new File(bundleDir, "com.zutubi.bundles.producer_3.0.0.jar");
        consumer1 = new File(bundleDir, "com.zutubi.bundles.consumer_1.0.0.jar");

        failonstartup = new File(bundleDir, "com.zutubi.bundles.failonstartup_1.0.0.jar");

        // should rename this - this is an empty file..
        bad = new File(bundleDir, "bad.jar");
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    // test installation.

    public void testInstallPlugin() throws Exception
    {
        startupPluginCore();

        File installedPluginFile = new File(paths.getPluginStorageDir(), "com.zutubi.bundles.producer_1.0.0.jar");
        assertFalse(installedPluginFile.isFile());

        Plugin installedPlugin = manager.install(producer1.toURI());
        assertPlugin(installedPlugin, PRODUCER_ID, "1.0.0", Plugin.State.ENABLED);
        assertEquals(1, manager.getPlugins().size());

        // verify that the plugin has been installed.
        assertTrue(installedPluginFile.isFile());
        assertEquals(null, installedPlugin.getErrorMessage());
        assertEquals(1, manager.equinox.getBundleCount(PRODUCER_ID));

        assertEquals(0, paths.getPluginWorkDir().list().length);
        assertEquals(1, paths.getPluginStorageDir().list().length);
    }

    public void testInstallWithAutostartDisabled() throws Exception
    {
        startupPluginCore();

        Plugin installedPlugin = manager.install(producer1.toURI(), false);
        assertPlugin(installedPlugin, PRODUCER_ID, "1.0.0", Plugin.State.DISABLED);
        assertEquals(null, installedPlugin.getErrorMessage());

        assertEquals(0, manager.equinox.getBundleCount(PRODUCER_ID));
    }

    public void testInstallWithAutostartEnabled() throws Exception
    {
        startupPluginCore();

        Plugin installedPlugin = manager.install(producer1.toURI(), true);
        assertPlugin(installedPlugin, PRODUCER_ID, "1.0.0", Plugin.State.ENABLED);
        assertEquals(null, installedPlugin.getErrorMessage());

        assertEquals(1, manager.equinox.getBundleCount(PRODUCER_ID));
    }

    public void testInstallWithMissingDependency() throws Exception
    {
        startupPluginCore();

        // install the consumer before we install the producer.
        Plugin installedConsumer = manager.install(consumer1.toURI());
        assertPlugin(installedConsumer, CONSUMER_ID, "1.0.0", Plugin.State.DISABLED);
        assertEquals("Failed to resolve bundle dependencies.", installedConsumer.getErrorMessage());
        //TODO: Improve the error message.
//        assertTrue(installedConsumer.getStatusMessage().contains("Reason: Missing Constraint: Require-Bundle: com.zutubi.bundles.producer; bundle-version=\"[1.0.0,2.0.0)\""));

        assertEquals(0, manager.equinox.getBundleCount(CONSUMER_ID));
    }

    public void testInstallingAPluginBeforeInstallingItsDependency() throws PluginException
    {
        startupPluginCore();

        // install the consumer before we install the producer.
        Plugin installedConsumer = manager.install(consumer1.toURI());
        assertPlugin(installedConsumer, CONSUMER_ID, "1.0.0", Plugin.State.DISABLED);
        assertEquals("Failed to resolve bundle dependencies.", installedConsumer.getErrorMessage());
        //TODO: Improve the error message.
//        assertTrue(installedConsumer.getStatusMessage().contains("Reason: Missing Constraint: Require-Bundle: com.zutubi.bundles.producer; bundle-version=\"[1.0.0,2.0.0)\""));
        assertTrue(new File(paths.getPluginStorageDir(), consumer1.getName()).isFile());

        // install the missing consumer dependency
        Plugin installedProducer = manager.install(producer1.toURI());
        assertPlugin(installedProducer, PRODUCER_ID, "1.0.0", Plugin.State.ENABLED);
        assertTrue(new File(paths.getPluginStorageDir(), producer1.getName()).isFile());

        // and ensure that we can now start the consumer.
        installedConsumer.enable();
        assertPlugin(installedConsumer, CONSUMER_ID, "1.0.0", Plugin.State.ENABLED);
        assertEquals(null, installedConsumer.getErrorMessage());

        assertEquals(1, manager.equinox.getBundleCount(CONSUMER_ID));
        assertEquals(1, manager.equinox.getBundleCount(PRODUCER_ID));
    }

    public void testEnablePlugin() throws Exception
    {
        startupPluginCore();

        Plugin plugin = manager.install(producer1.toURI(), false);
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.DISABLED);
        assertEquals(0, manager.equinox.getBundleCount(PRODUCER_ID));

        plugin.enable();
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.ENABLED);
        assertEquals(1, manager.equinox.getBundleCount(PRODUCER_ID));
    }

    public void testDisablePlugin() throws Exception
    {
        startupPluginCore();

        Plugin plugin = manager.install(producer1.toURI());
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.ENABLED);

        plugin.disable();
        assertEquals(Plugin.State.DISABLING, plugin.getState());

        // restart the plugin system.
        restartPluginCore();

        // the plugin should be disabled after restart.
        plugin = manager.getPlugin(PRODUCER_ID);
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.DISABLED);
    }

    public void testEnableDisableCorrectlyPersistsAcrossRestarts() throws Exception
    {
        startupPluginCore();

        Plugin plugin = manager.install(producer1.toURI(), false);
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.DISABLED);
        assertEquals(0, manager.equinox.getBundleCount(PRODUCER_ID));

        restartPluginCore();
        plugin = manager.getPlugin(PRODUCER_ID);
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.DISABLED);
        assertEquals(0, manager.equinox.getBundleCount(PRODUCER_ID));

        plugin.enable();
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.ENABLED);
        assertEquals(1, manager.equinox.getBundleCount(PRODUCER_ID));

        restartPluginCore();
        plugin = manager.getPlugin(PRODUCER_ID);
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.ENABLED);
        assertEquals(1, manager.equinox.getBundleCount(PRODUCER_ID));
    }

    public void testDisablingRequiredPluginAlsoDisablesDependent() throws Exception
    {
        startupPluginCore();

        Plugin producer = manager.install(producer1.toURI());
        assertPlugin(producer, PRODUCER_ID, "1.0.0", Plugin.State.ENABLED);
        Plugin consumer = manager.install(consumer1.toURI());
        assertPlugin(consumer, CONSUMER_ID, "1.0.0", Plugin.State.ENABLED);

        producer.disable();
        assertEquals(Plugin.State.DISABLING, producer.getState());

        restartPluginCore();

        producer = manager.getPlugin(PRODUCER_ID);
        assertPlugin(producer, PRODUCER_ID, "1.0.0", Plugin.State.DISABLED);
        consumer = manager.getPlugin(CONSUMER_ID);
        assertPlugin(consumer, CONSUMER_ID, "1.0.0", Plugin.State.DISABLED);

        producer.enable();
        assertPlugin(producer, PRODUCER_ID, "1.0.0", Plugin.State.ENABLED);

        //FIXME: should we be restarting any dependent plugins that are marked in the registry as ENABLED?

        restartPluginCore();
        producer = manager.getPlugin(PRODUCER_ID);
        assertPlugin(producer, PRODUCER_ID, "1.0.0", Plugin.State.ENABLED);
        consumer = manager.getPlugin(CONSUMER_ID);
        assertPlugin(consumer, CONSUMER_ID, "1.0.0", Plugin.State.ENABLED);
    }

    public void testUpgradeDisabledPlugin() throws Exception
    {
        startupPluginCore();

        Plugin plugin = manager.install(producer1.toURI(), false);
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.DISABLED);
        assertEquals(1, manager.getPlugins().size());

        // upgraded plugin should retain the state of the original. So, upgrading a disabled plugin
        // will remain disabled.
        plugin = plugin.upgrade(producer11.toURI());
        assertPlugin(plugin, PRODUCER_ID, "1.1.0", Plugin.State.DISABLED);
        assertEquals(1, manager.getPlugins().size());

        // ensure that the correct plugin is cached in the core.
        plugin = manager.getPlugin(PRODUCER_ID);
        assertPlugin(plugin, PRODUCER_ID, "1.1.0", Plugin.State.DISABLED);

        restartPluginCore();

        // ensure that the correct plugin is available on restart.
        plugin = manager.getPlugin(PRODUCER_ID);
        assertPlugin(plugin, PRODUCER_ID, "1.1.0", Plugin.State.DISABLED);

        // it is not until we attempt to enable the plugin that the version change is detected.
        plugin.enable();
        assertPlugin(plugin, PRODUCER_ID, "1.1.0", Plugin.State.ENABLED);
    }

    public void testUpgradeEnabledPlugin() throws Exception
    {
        startupPluginCore();

        Plugin plugin = manager.install(producer1.toURI());
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.ENABLED);

        // upgrading an enabled plugin requires a core restart.
        plugin = plugin.upgrade(producer11.toURI());
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.UPDATING);

        // restart the plugin system.
        restartPluginCore();

        plugin = manager.getPlugin(PRODUCER_ID);
        assertPlugin(plugin, PRODUCER_ID, "1.1.0", Plugin.State.VERSION_CHANGE);
        assertEquals(1, manager.equinox.getBundleCount(PRODUCER_ID));

        plugin.resolve();

        assertEquals(Plugin.State.ENABLED, plugin.getState());
        assertEquals(1, manager.equinox.getBundleCount(PRODUCER_ID));
    }

    public void testMultiplePluginUpgrades() throws Exception
    {
        startupPluginCore();

        Plugin plugin = manager.install(producer1.toURI());
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.ENABLED);
        assertEquals(0, paths.getPluginWorkDir().list().length);

        plugin = plugin.upgrade(producer11.toURI());
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.UPDATING);
        assertEquals(1, paths.getPluginWorkDir().list().length);
        assertTrue(new File(paths.getPluginWorkDir(), producer11.getName()).isFile());

        plugin = plugin.upgrade(producer2.toURI());
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.UPDATING);
        assertEquals(1, paths.getPluginWorkDir().list().length);
        assertTrue(new File(paths.getPluginWorkDir(), producer2.getName()).isFile());

        // restart the plugin system.
        restartPluginCore();

        plugin = manager.getPlugin(PRODUCER_ID);
        assertPlugin(plugin, PRODUCER_ID, "2.0.0", Plugin.State.VERSION_CHANGE);

        plugin.resolve();

        assertPlugin(plugin, PRODUCER_ID, "2.0.0", Plugin.State.ENABLED);

        assertEquals(0, paths.getPluginWorkDir().list().length);
        assertEquals(1, paths.getPluginStorageDir().list().length);
    }

    public void testManualUpgradeOfEnabledPlugin() throws Exception
    {
        startupPluginCore();

        Plugin plugin = manager.install(producer1.toURI());
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.ENABLED);

        shutdownPluginCore();

        // manually upgrade the plugin in teh plugin store directory.
        assertTrue(new File(paths.getPluginStorageDir(), producer1.getName()).delete());
        FileSystemUtils.copy(paths.getPluginStorageDir(), producer11);

        startupPluginCore();

        plugin = manager.getPlugin(PRODUCER_ID);
        assertPlugin(plugin, PRODUCER_ID, "1.1.0", Plugin.State.VERSION_CHANGE);
        assertEquals(1, manager.equinox.getBundleCount(PRODUCER_ID));

        plugin.resolve();
        assertPlugin(plugin, PRODUCER_ID, "1.1.0", Plugin.State.ENABLED);
        assertEquals(1, manager.equinox.getBundleCount(PRODUCER_ID));
    }

    public void testLoadInternalPlugins() throws Exception
    {
        FileSystemUtils.copy(paths.getInternalPluginStorageDir(), producer1);

        startupPluginCore();

        Plugin plugin = manager.getInternalPlugin(PRODUCER_ID);
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.ENABLED);
    }

    public void testLoadPrepackagedPlugins() throws Exception
    {
        FileSystemUtils.copy(paths.getPrepackagedPluginStorageDir(), producer1);

        startupPluginCore();

        Plugin plugin = manager.getPlugin(PRODUCER_ID);
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.ENABLED);
    }

    public void testUpgradePrepackagedPlugins() throws Exception
    {
        FileSystemUtils.copy(paths.getPrepackagedPluginStorageDir(), producer1);

        startupPluginCore();

        Plugin plugin = manager.getPlugin(PRODUCER_ID);
        assertPlugin(plugin, PRODUCER_ID, "1.0.0", Plugin.State.ENABLED);

        shutdownPluginCore();

        // add upgrade to the prepackaged plugin directory
        FileSystemUtils.cleanOutputDir(paths.getPrepackagedPluginStorageDir());
        FileSystemUtils.copy(paths.getPrepackagedPluginStorageDir(), producer11);

        startupPluginCore();

        plugin = manager.getPlugin(PRODUCER_ID);
        assertPlugin(plugin, PRODUCER_ID, "1.1.0", Plugin.State.VERSION_CHANGE);

        plugin.resolve();
        assertPlugin(plugin, PRODUCER_ID, "1.1.0", Plugin.State.ENABLED);
    }

    public void testDoNotUpgradeUninstalledPrepackagedPlugins() throws Exception
    {
        startupPluginCore();

        Plugin plugin = manager.install(producer1.toURI(), false);
        plugin.uninstall();
        assertEquals(Plugin.State.UNINSTALLED, plugin.getState());

        shutdownPluginCore();

        FileSystemUtils.copy(paths.getPrepackagedPluginStorageDir(), producer11);

        startupPluginCore();

        // verify that the plugin is still uninstalled, and is still the old version.
        plugin = manager.getPlugin(PRODUCER_ID);
        assertNull(plugin);
    }

    public void testUpgradeInternalPlugin() throws Exception
    {
        // internal plugins 'just upgrade'.  None of the standard processing applies.
        
        FileSystemUtils.copy(paths.getInternalPluginStorageDir(), producer1);

        startupPluginCore();

        Plugin plugin = manager.getInternalPlugin(PRODUCER_ID);
        assertNotNull(plugin);
        assertEquals(Plugin.State.ENABLED, plugin.getState());

        shutdownPluginCore();

        // upgrade the internally packaged plugin.
        FileSystemUtils.cleanOutputDir(paths.getInternalPluginStorageDir());
        FileSystemUtils.copy(paths.getInternalPluginStorageDir(), producer11);

        startupPluginCore();

        plugin = manager.getInternalPlugin(PRODUCER_ID);
        assertNotNull(plugin);
        assertEquals(Plugin.State.ENABLED, plugin.getState());
    }

    public void testUninstallPlugin() throws Exception
    {
        startupPluginCore();

        Plugin plugin = manager.install(producer1.toURI());
        assertEquals(Plugin.State.ENABLED, plugin.getState());
        assertEquals("1.0.0", plugin.getVersion().toString());

        plugin.uninstall();
        assertEquals(Plugin.State.UNINSTALLING, plugin.getState());

        restartPluginCore();

        plugin = manager.getPlugin(PRODUCER_ID);
        assertNull(plugin);
    }

    public void testUninstallDisabledPlugin() throws Exception
    {
        startupPluginCore();

        Plugin plugin = manager.install(producer1.toURI(), false);
        assertEquals(Plugin.State.DISABLED, plugin.getState());

        plugin.uninstall();
        assertEquals(Plugin.State.UNINSTALLED, plugin.getState());

        restartPluginCore();

        plugin = manager.getPlugin(PRODUCER_ID);
        assertNull(plugin);
    }

    public void testReinstallAfterUninstall() throws Exception
    {
        startupPluginCore();

        Plugin plugin = manager.install(producer1.toURI());
        assertEquals(Plugin.State.ENABLED, plugin.getState());

        plugin.uninstall();
        restartPluginCore();
        plugin = manager.getPlugin(PRODUCER_ID);
        assertNull(plugin);

        plugin = manager.install(producer1.toURI());
        assertNotNull(plugin);
        assertEquals(Plugin.State.ENABLED, plugin.getState());
    }

    public void testManualUninstall() throws Exception
    {
        startupPluginCore();

        Plugin plugin = manager.install(producer1.toURI());
        assertEquals(Plugin.State.ENABLED, plugin.getState());

        shutdownPluginCore();

        FileSystemUtils.delete(new File(paths.getPluginStorageDir(), producer1.getName()));

        startupPluginCore();

        plugin = manager.getPlugin(PRODUCER_ID);
        assertNull(plugin);
    }

    public void testCanNotUninstallInternalPlugin() throws Exception
    {
        FileSystemUtils.copy(paths.getInternalPluginStorageDir(), producer1);

        startupPluginCore();

        Plugin plugin = manager.getInternalPlugin(PRODUCER_ID);
        assertNotNull(plugin);
        try
        {
            plugin.uninstall();
            fail();
        }
        catch (PluginException e)
        {
            assertEquals("Cannot uninstall plugin: this is an internal plugin.", e.getMessage());
        }
    }

    public void testDependentPlugins() throws PluginException
    {
        startupPluginCore();

        Plugin producer = manager.install(producer1.toURI());
        List<Plugin> dependentPlugins = producer.getDependentPlugins();
        assertEquals(0, dependentPlugins.size());

        Plugin consumer = manager.install(consumer1.toURI());
        dependentPlugins = producer.getDependentPlugins();
        assertEquals(1, dependentPlugins.size());
        assertEquals(consumer, dependentPlugins.get(0));
    }

    public void testPluginDependenciesForInstalledPlugins() throws Exception
    {
        startupPluginCore();

        // this differs from the previous in that we install and then restart the plugin manager.  This
        // better tests the init processing.
        Plugin producer = manager.install(producer1.toURI());
        Plugin consumer = manager.install(consumer1.toURI());

        restartPluginCore();

        List<Plugin> dependentPlugins = producer.getDependentPlugins();
        assertEquals(1, dependentPlugins.size());
        assertEquals(consumer, dependentPlugins.get(0));
    }

    public void testGetRequiredPlugins() throws PluginException
    {
        startupPluginCore();

        Plugin producer = manager.install(producer1.toURI());
        Plugin consumer = manager.install(consumer1.toURI());

        List<PluginRequirement> pluginRequirements = consumer.getRequiredPlugins();
        assertEquals(1, pluginRequirements.size());
        assertEquals(PRODUCER_ID, pluginRequirements.get(0).getId());
        assertEquals("[1.0.0, 2.0.0)", pluginRequirements.get(0).getVersionRange().toString());
        assertEquals(producer, pluginRequirements.get(0).getSupplier());
    }

    public void testInternalPluginsAreNotRegistered() throws IOException, PluginException
    {
        FileSystemUtils.copy(paths.getInternalPluginStorageDir(), producer1);
        startupPluginCore();

        PluginRegistry registry = manager.getPluginRegistry();
        assertEquals(0, registry.getRegistrations().size());
        assertFalse(registry.isRegistered("com.zutubi.bundles.producer"));
    }

    public void testNonInternalPluginsAreRegistered() throws PluginException, IOException
    {
        FileSystemUtils.copy(paths.getPluginStorageDir(), producer1);
        startupPluginCore();

        PluginRegistry registry = manager.getPluginRegistry();
        assertEquals(1, registry.getRegistrations().size());
        assertTrue(registry.isRegistered("com.zutubi.bundles.producer"));
    }


    public void testConcurrentUpgrades()
    {
        // upgrade 2 different plugins,
    }

    public void testUpgradingToPluginWhereFileWasDeletedBeforeRestart()
    {
        // upgrade a plugin, -> copied into the working directory.
        // working directory is cleared out (is scratch space after all)
        // restart
        // plugin upgrade fails, but old plugin should be activated - appropriate message should be available somewhere.
    }

    public void testUpgradingDisabledPluginTriggersUpgrade()
    {
        // install producer 1.0.0,
        // disable producer
        // upgrade to producer 3.0.0
        // ensure that upgrade is required.
    }

    public void testDependencyCheckMessagesOnInstall() throws PluginException
    {
        startupPluginCore();

        Plugin consumer = manager.install(consumer1.toURI());
        assertEquals("Failed to resolve bundle dependencies.", consumer.getErrorMessage());
        assertEquals(Plugin.State.DISABLED, consumer.getState());

        //TODO: should be identifying the missing dependency
    }

    public void testDependencyCheckMessagesOnStartup() throws PluginException, IOException
    {
        FileSystemUtils.copy(paths.getPluginStorageDir(), consumer1);

        startupPluginCore();

        Plugin consumer = manager.getPlugin("com.zutubi.bundles.consumer");
        assertEquals("Failed to resolve bundle.", consumer.getErrorMessage());
        assertEquals(Plugin.State.DISABLED, consumer.getState());

        //TODO: should be identifying the missing dependency
    }

    public void testInstallingZeroLengthJarFile() throws Exception
    {
        startupPluginCore();

        try
        {
            manager.install(bad.toURI());
            fail();
        }
        catch (PluginException e)
        {
            // exception expected.  Not sure if this is the best response to this type of error.
        }

        // ensure that it is not left behind.
        File storage = paths.getPluginStorageDir();
        assertEquals(0, storage.list().length);
    }

    public void testPluginThatFailsOnStartup() throws PluginException
    {
        startupPluginCore();

        Plugin plugin = manager.install(failonstartup.toURI());
        assertEquals(Plugin.State.DISABLED, plugin.getState());
        assertNotNull(plugin.getErrorMessage());
    }

    public void testPluginThatFailsOnStartup_ManualInstall() throws IOException, PluginException
    {
        FileSystemUtils.copy(paths.getPluginStorageDir(), failonstartup);

        startupPluginCore();

        Plugin plugin = manager.getPlugin("com.zutubi.bundles.error.ErrorOnStartup");
        assertEquals(Plugin.State.DISABLED, plugin.getState());
        assertNotNull(plugin.getErrorMessage());
    }

    public void testPluginThatFailsOnStartupWillRetryStartupOnNextSystemStartup() throws Exception
    {
        FileSystemUtils.copy(paths.getPluginStorageDir(), failonstartup);

        startupPluginCore();

        Plugin plugin = manager.getPlugin("com.zutubi.bundles.error.ErrorOnStartup");
        assertEquals(Plugin.State.DISABLED, plugin.getState());

        restartPluginCore();

        plugin = manager.getPlugin("com.zutubi.bundles.error.ErrorOnStartup");
        assertEquals(Plugin.State.DISABLED, plugin.getState());
    }

    private void assertPlugin(Plugin plugin, String expectedId, String expectedVersion, Plugin.State expectedState)
    {
        assertNotNull(plugin);
        assertEquals(expectedId, plugin.getId());
        assertEquals(expectedVersion, plugin.getVersion().toString());
        assertEquals(expectedState, plugin.getState());
    }
}
