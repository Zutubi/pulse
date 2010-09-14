package com.zutubi.pulse.acceptance.plugins;

import com.zutubi.pulse.acceptance.AcceptanceTestUtils;
import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PluginPaths;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.plugins.PluginUpgradeManager;
import com.zutubi.pulse.master.upgrade.DefaultUpgradeManager;
import com.zutubi.pulse.master.upgrade.UpgradeTask;
import com.zutubi.pulse.master.upgrade.UpgradeableComponent;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.bean.ObjectFactory;

import java.io.File;
import java.util.List;

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

    protected void setUp() throws Exception
    {
        super.setUp();

        // base directory will be cleaned up at the end of the test.
        tmpDir = FileSystemUtils.createTempDir(getClass().getName(), "");

        File pkgFile = AcceptanceTestUtils.getPulsePackage();

        pluginSystem = new PluginSystem(pkgFile, tmpDir);
        paths = pluginSystem.getPluginPaths();

        producer1 = getInputFile("com.zutubi.bundles.producer_1.0.0", "jar");
        producer2 = getInputFile("com.zutubi.bundles.producer_2.0.0", "jar");
        producer3 = getInputFile("com.zutubi.bundles.producer_3.0.0", "jar");

        objectFactory = new DefaultObjectFactory();
    }

    protected void tearDown() throws Exception
    {
        pluginSystem.shutdown();
        removeDirectory(tmpDir);

        super.tearDown();
    }

    public void testPluginUpgradeSanityCheck() throws Exception
    {
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

        Plugin plugin = pluginSystem.getPluginManager().getPlugin("com.zutubi.bundles.producer");
        assertEquals(Plugin.State.ENABLED, plugin.getState());
        plugin = pluginSystem.getPluginManager().getPlugin("com.zutubi.pulse.core.commands.ant");
        assertEquals(Plugin.State.ENABLED, plugin.getState());

        restartPluginCore();

        assertFalse(pluginUpgradeManager.isUpgradeRequired());
    }

    public void testRequestUpgradeViaPluginInterface() throws Exception
    {
        FileSystemUtils.copy(paths.getPluginStorageDir(), producer2);

        startupPluginCore();

        assertFalse(pluginUpgradeManager.isUpgradeRequired());

        Plugin plugin = pluginSystem.getPluginManager().getPlugin("com.zutubi.bundles.producer");
        plugin.upgrade(producer3.toURI());
        assertEquals(Plugin.State.UPGRADING, plugin.getState());

        restartPluginCore();

        assertTrue(pluginUpgradeManager.isUpgradeRequired());
        plugin = pluginSystem.getPluginManager().getPlugin("com.zutubi.bundles.producer");
        assertEquals(Plugin.State.ENABLED, plugin.getState());
        assertEquals("3.0.0", plugin.getVersion().toString());
    }

    public void testInPlaceNew() throws Exception
    {
        assertNewPluginHandledCorrectly(paths.getPluginStorageDir());
    }

    public void testInPlaceNoUpgrade() throws Exception
    {
        assertNoUpgradeHandledCorrectly(paths.getPluginStorageDir());
    }

    public void testInPlaceUpgrade() throws Exception
    {
        assertUpgradeHandledCorrectly(paths.getPluginStorageDir());
    }

    public void testInternalNew() throws Exception
    {
        assertNewPluginHandledCorrectly(paths.getInternalPluginStorageDir());
    }

    public void testInternalNoUpgrade() throws Exception
    {
        assertNoUpgradeHandledCorrectly(paths.getInternalPluginStorageDir());
    }

    public void testPrepackagedNew() throws Exception
    {
        assertNewPluginHandledCorrectly(paths.getPrepackagedPluginStorageDir());
    }

    public void testPrepackagedNoUpgrade() throws Exception
    {
        assertNoUpgradeHandledCorrectly(paths.getPrepackagedPluginStorageDir());
    }

    public void testPrepackagedUpgrade() throws Exception
    {
        assertUpgradeHandledCorrectly(paths.getPrepackagedPluginStorageDir());
    }

    private void assertNewPluginHandledCorrectly(File dir) throws Exception
    {
        startupPluginCore();
        shutdownPluginCore();

        FileSystemUtils.copy(dir, producer1);

        startupPluginCore();

        assertFalse(pluginUpgradeManager.isUpgradeRequired());

        Plugin plugin = pluginSystem.getPluginManager().getPlugin("com.zutubi.bundles.producer");
        if (plugin != null)
        {
            assertEquals(Plugin.State.ENABLED, plugin.getState());
        }
    }

    private void assertNoUpgradeHandledCorrectly(File dir) throws Exception
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

        assertFalse(pluginUpgradeManager.isUpgradeRequired());

        Plugin plugin = pluginSystem.getPluginManager().getPlugin("com.zutubi.bundles.producer");
        if (plugin != null)
        {
            assertEquals(Plugin.State.ENABLED, plugin.getState());
        }
    }

    private void assertUpgradeHandledCorrectly(File dir) throws Exception
    {
        if (!dir.exists())
        {
            assertTrue(dir.mkdirs());
        }
        FileSystemUtils.copy(dir, producer2);

        startupPluginCore();

        assertEquals("2.0.0", pluginSystem.getPluginManager().getPluginRegistry().getEntry("com.zutubi.bundles.producer").get(PluginUpgradeManager.PLUGIN_VERSION_KEY));

        shutdownPluginCore();

        FileSystemUtils.delete(new File(dir, producer2.getName()));
        FileSystemUtils.copy(dir, producer3);

        startupPluginCore();
        
        assertTrue(pluginUpgradeManager.isUpgradeRequired());
        assertEquals("2.0.0", pluginSystem.getPluginManager().getPluginRegistry().getEntry("com.zutubi.bundles.producer").get(PluginUpgradeManager.PLUGIN_VERSION_KEY));
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

