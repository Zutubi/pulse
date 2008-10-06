package com.zutubi.pulse.acceptance.plugins;

import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginManagerTest;
import com.zutubi.pulse.core.plugins.PluginPaths;
import com.zutubi.pulse.core.plugins.PluginRegistryEntry;
import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.pulse.master.plugins.PluginUpgradeManager;
import com.zutubi.pulse.master.upgrade.DefaultUpgradeManager;
import com.zutubi.pulse.master.upgrade.UpgradeTask;
import com.zutubi.pulse.master.upgrade.UpgradeableComponent;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.bean.ObjectFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

/**
 *
 *
 */
@Test
public class PluginUpgradeManagerAcceptanceTest extends PulseTestCase
{
    private PluginUpgradeManager pluginUpgradeManager;
    private ObjectFactory objectFactory;

    private File producer1;
    private File producer2;
    private File producer3;
    private File tmpDir;
    private PluginPaths paths;

    private PluginSystem pluginSystem;

    @BeforeMethod
    protected void setUp() throws Exception
    {
        super.setUp();

        // base directory will be cleaned up at the end of the test.
        tmpDir = FileSystemUtils.createTempDir(PluginManagerTest.class.getName(), "");

        File pkgFile = getPulsePackage();
/*
        if (pkgFile == null)
        {
            pkgFile = new File("test-packages/pulse-2.0.9.zip");
        }
*/

        pluginSystem = new PluginSystem(pkgFile, tmpDir);
        paths = pluginSystem.getPluginPaths();

        File bundleDir = new File(getPulseRoot(), FileSystemUtils.composeFilename("com.zutubi.pulse.core", "src", "test", "com", "zutubi", "pulse", "core", "plugins", "test-bundles"));
        producer1 = new File(bundleDir, "com.zutubi.bundles.producer_1.0.0.jar");
        producer2 = new File(bundleDir, "com.zutubi.bundles.producer_2.0.0.jar");
        producer3 = new File(bundleDir, "com.zutubi.bundles.producer_3.0.0.jar");

        objectFactory = new DefaultObjectFactory();
    }

    @AfterMethod
    protected void tearDown() throws Exception
    {
        pluginSystem.shutdown();
        pluginSystem = null;
        paths = null;

        objectFactory = null;

        producer1 = null;
        producer2 = null;
        producer3 = null;

        removeDirectory(tmpDir);
        tmpDir = null;
        
        super.tearDown();
    }

    public void testPluginUpgradeSanityCheck() throws Exception
    {
        assertTrue(paths.getPluginStorageDir().mkdirs());
        FileSystemUtils.copy(paths.getPluginStorageDir(), producer2);

        startupPluginCore();

        assertFalse(pluginUpgradeManager.isUpgradeRequired());

        shutdownPluginCore();

        // replace 2.0.0 with 3.0.0
        FileSystemUtils.delete(new File(paths.getPluginStorageDir(), producer2.getName()));
        FileSystemUtils.copy(paths.getPluginStorageDir(), producer3);

        startupPluginCore();

        assertTrue(pluginUpgradeManager.isUpgradeRequired());

        List<UpgradeableComponent> upgradeableComponents = pluginUpgradeManager.getUpgradeableComponents();
        assertNotNull(upgradeableComponents);
        assertEquals(1, upgradeableComponents.size());

        UpgradeableComponent upgradeableComponent = upgradeableComponents.get(0);
        assertTrue(upgradeableComponent.isUpgradeRequired());

        List<UpgradeTask> upgradeTasks = upgradeableComponent.getUpgradeTasks();
        assertNotNull(upgradeTasks);
        assertEquals(1, upgradeTasks.size());

        UpgradeTask upgradeTask = upgradeTasks.get(0);
        assertEquals("com.zutubi.bundles.producer.ProducerUpgradeTask", upgradeTask.getClass().getName());

        // execute upgrade.
        DefaultUpgradeManager upgradeManager = new DefaultUpgradeManager();
        upgradeManager.add(pluginUpgradeManager);

        assertTrue(upgradeManager.isUpgradeRequired());
        upgradeManager.prepareUpgrade();
        upgradeManager.executeUpgrade();

        // restart plugin core
        restartPluginCore();

        assertFalse(pluginUpgradeManager.isUpgradeRequired());
    }

    public void testRequestUpgradeViaPluginInterface() throws Exception
    {
        assertTrue(paths.getPluginStorageDir().mkdirs());
        FileSystemUtils.copy(paths.getPluginStorageDir(), producer2);

        startupPluginCore();

        assertFalse(pluginUpgradeManager.isUpgradeRequired());

        Plugin plugin = pluginSystem.getPluginManager().getPlugin("com.zutubi.bundles.producer");
        plugin.upgrade(producer3.toURI());
        assertEquals(Plugin.State.UPDATING, plugin.getState());

        restartPluginCore();

        assertTrue(pluginUpgradeManager.isUpgradeRequired());

        plugin = pluginSystem.getPluginManager().getPlugin("com.zutubi.bundles.producer");
        assertEquals(Plugin.State.VERSION_CHANGE, plugin.getState());
    }

    public void testInPlaceNew() throws Exception
    {
        assertNewPluginHandledCorrectly(paths.getPluginStorageDir());
    }

    public void testInPlaceNoUpgrade() throws Exception
    {
        assertNoUpgradeHandledCorrectly(paths.getPluginStorageDir(), false);
    }

    public void testInPlaceUpgrade() throws Exception
    {
        assertUpgradeHandledCorrectly(paths.getPluginStorageDir(), false);
    }

    public void testInternalNew() throws Exception
    {
        assertNewPluginHandledCorrectly(paths.getInternalPluginStorageDir());
    }

    public void testInternalNoUpgrade() throws Exception
    {
        assertNoUpgradeHandledCorrectly(paths.getInternalPluginStorageDir(), true);
    }

    public void testPrepackagedNew() throws Exception
    {
        assertNewPluginHandledCorrectly(paths.getPrepackagedPluginStorageDir());
    }

    public void testPrepackagedNoUpgrade() throws Exception
    {
        assertNoUpgradeHandledCorrectly(paths.getPrepackagedPluginStorageDir(), false);
    }

    public void testPrepackagedUpgrade() throws Exception
    {
        assertUpgradeHandledCorrectly(paths.getPrepackagedPluginStorageDir(), false);
    }

    private void assertNewPluginHandledCorrectly(File dir) throws Exception
    {
        startupPluginCore();
        shutdownPluginCore();

        FileSystemUtils.copy(dir, producer1);

        startupPluginCore();

        assertFalse(pluginSystem.getPluginManager().isVersionChangeDetected());
        assertFalse(pluginUpgradeManager.isUpgradeRequired());

        Plugin plugin = pluginSystem.getPluginManager().getInternalPlugin("com.zutubi.bundles.producer");
        if (plugin == null)
        {
            plugin = pluginSystem.getPluginManager().getPlugin("com.zutubi.bundles.producer");
        }
        
        assertEquals(Plugin.State.ENABLED, plugin.getState());
    }

    private void assertNoUpgradeHandledCorrectly(File dir, boolean internal) throws Exception
    {
        if (!dir.exists())
        {
            assertTrue(dir.mkdirs());
        }
        FileSystemUtils.copy(dir, producer1);

        startupPluginCore();

        assertFalse(pluginUpgradeManager.isUpgradeRequired());

        shutdownPluginCore();

        FileSystemUtils.delete(new File(dir, producer1.getName()));
        FileSystemUtils.copy(dir, producer2);

        startupPluginCore();

        if (!internal)
        {
            assertTrue(pluginSystem.getPluginManager().isVersionChangeDetected());
        }

        assertFalse(pluginUpgradeManager.isUpgradeRequired());

        Plugin plugin = pluginSystem.getPluginManager().getInternalPlugin("com.zutubi.bundles.producer");
        if (plugin == null)
        {
            plugin = pluginSystem.getPluginManager().getPlugin("com.zutubi.bundles.producer");
        }
        assertEquals(Plugin.State.ENABLED, plugin.getState());
    }

    private void assertUpgradeHandledCorrectly(File dir, boolean internal) throws Exception
    {
        if (!dir.exists())
        {
            assertTrue(dir.mkdirs());
        }
        FileSystemUtils.copy(dir, producer2);

        startupPluginCore();

        assertEquals("2.0.0", pluginSystem.getPluginManager().getPluginRegistry().getEntry("com.zutubi.bundles.producer").get(PluginRegistryEntry.PLUGIN_VERSION_KEY));

        shutdownPluginCore();

        FileSystemUtils.delete(new File(dir, producer2.getName()));
        FileSystemUtils.copy(dir, producer3);

        startupPluginCore();
        
        if (!internal)
        {
            assertTrue(pluginSystem.getPluginManager().isVersionChangeDetected());
        }

        assertTrue(pluginUpgradeManager.isUpgradeRequired());
        assertEquals("2.0.0", pluginSystem.getPluginManager().getPluginRegistry().getEntry("com.zutubi.bundles.producer").get(PluginRegistryEntry.PLUGIN_VERSION_KEY));
    }

    private void restartPluginCore() throws Exception
    {
        shutdownPluginCore();
        startupPluginCore();
    }

    private void startupPluginCore() throws Exception
    {
        pluginSystem.startup();

        pluginUpgradeManager = new PluginUpgradeManager();
        pluginUpgradeManager.setPluginManager(pluginSystem.getPluginManager());
        pluginUpgradeManager.setObjectFactory(objectFactory);
        pluginUpgradeManager.init();
    }

    protected void shutdownPluginCore() throws Exception
    {
        pluginSystem.shutdown();
        pluginUpgradeManager = null;
    }
}

