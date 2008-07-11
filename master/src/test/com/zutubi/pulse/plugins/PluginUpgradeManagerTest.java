package com.zutubi.pulse.plugins;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.upgrade.UpgradeableComponent;
import com.zutubi.pulse.upgrade.UpgradeTask;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.bean.DefaultObjectFactory;

import java.io.File;
import java.util.List;

/**
 *
 *
 */
public class PluginUpgradeManagerTest extends BasePluginSystemTestCase
{
    private PluginUpgradeManager pluginUpgradeManager;
    private ObjectFactory objectFactory;

    private File producer1;
    private File producer2;
    private File producer3;

    protected void setUp() throws Exception
    {
        super.setUp();

        File bundleDir = getTestDataDir("core", "test-bundles");
        producer1 = new File(bundleDir, "com.zutubi.bundles.producer_1.0.0.jar");
        producer2 = new File(bundleDir, "com.zutubi.bundles.producer_2.0.0.jar");
        producer3 = new File(bundleDir, "com.zutubi.bundles.producer_3.0.0.jar");

        objectFactory = new DefaultObjectFactory();
    }

    protected void tearDown() throws Exception
    {
        pluginUpgradeManager = null;
        objectFactory = null;

        super.tearDown();
    }

    public void testNoop()
    {

    }

    public void disabledTestDetectingUpgrade() throws Exception
    {
        // install the equinox registry plugins to enable all of the registry goodness.
        // ok, this step needs some work since via acceptance tests, this is not so easy to define - we need another way
        // to get hold of these resources that are part of the build/runtime environment.
        installPulseInternalBundles();

        FileSystemUtils.copy(paths.getPluginStorageDir(), producer2);

        startupPluginCore();

        pluginUpgradeManager = new PluginUpgradeManager();
        pluginUpgradeManager.setPluginManager(manager);
        pluginUpgradeManager.setObjectFactory(objectFactory);
        pluginUpgradeManager.init();

        assertFalse(pluginUpgradeManager.isUpgradeRequired());

        shutdownPluginCore();

        // replace 2.0.0 with 3.0.0
        FileSystemUtils.delete(new File(paths.getPluginStorageDir(), producer2.getName()));
        FileSystemUtils.copy(paths.getPluginStorageDir(), producer3);

        startupPluginCore();

        pluginUpgradeManager = new PluginUpgradeManager();
        pluginUpgradeManager.setPluginManager(manager);
        pluginUpgradeManager.setObjectFactory(objectFactory);
        pluginUpgradeManager.init();

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

        // restart plugin core

        // verify that no up grade is required.
    }

}
