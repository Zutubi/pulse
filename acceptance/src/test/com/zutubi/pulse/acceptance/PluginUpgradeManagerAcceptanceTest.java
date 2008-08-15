package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.support.JythonPackageFactory;
import com.zutubi.pulse.acceptance.support.PackageFactory;
import com.zutubi.pulse.acceptance.support.Pulse;
import com.zutubi.pulse.acceptance.support.PulsePackage;
import com.zutubi.pulse.plugins.ConfigurablePluginPaths;
import com.zutubi.pulse.plugins.Plugin;
import com.zutubi.pulse.plugins.PluginException;
import com.zutubi.pulse.plugins.PluginManager;
import com.zutubi.pulse.plugins.PluginManagerTest;
import com.zutubi.pulse.plugins.PluginUpgradeManager;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.upgrade.DefaultUpgradeManager;
import com.zutubi.pulse.upgrade.UpgradeTask;
import com.zutubi.pulse.upgrade.UpgradeableComponent;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.bean.ObjectFactory;
import org.eclipse.core.internal.registry.osgi.OSGIUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    private ConfigurablePluginPaths paths;

    protected PluginManager pluginManager;

    @BeforeMethod
    protected void setUp() throws Exception
    {
        File pkgFile = getPulsePackage();
/*
        if (pkgFile == null)
        {
            pkgFile = new File("test-packages/pulse-2.0.9.zip");
        }
*/

        PackageFactory factory = new JythonPackageFactory();
        PulsePackage pkg = factory.createPackage(pkgFile);

        // base directory will be cleaned up at the end of the test.
        tmpDir = FileSystemUtils.createTempDir(PluginManagerTest.class.getName(), "");

        Pulse pulse = pkg.extractTo(tmpDir.getCanonicalPath());

        // Plugin paths configuration based on
        // a) are we dealing with an existing package.
        // b) are we running within IntelliJ.
        
        File internalDir = new File(pulse.getPluginRoot(), "internal");
        File prepackagedDir = new File(pulse.getPluginRoot(), "prepackaged");
        File osgiDir = new File(pulse.getPluginRoot(), "config");

        File storageDir = new File(tmpDir, "plugin/storage");
        assertTrue(storageDir.mkdirs());
        
        File workDir = new File(tmpDir, "plugin/work");
        assertTrue(workDir.mkdirs());

        paths = new ConfigurablePluginPaths();
        paths.setPrepackagedPluginStorageDir(prepackagedDir);
        paths.setInternalPluginStorageDir(internalDir);
        paths.setOsgiConfigurationDir(osgiDir);
        paths.setPluginRegistryDir(storageDir);
        paths.setPluginStorageDir(storageDir);
        paths.setPluginWorkDir(workDir);

        File bundleDir = new File(getPulseRoot(), FileSystemUtils.composeFilename("core", "src", "test", "com", "zutubi", "pulse", "plugins", "test-bundles"));
        producer1 = new File(bundleDir, "com.zutubi.bundles.producer_1.0.0.jar");
        producer2 = new File(bundleDir, "com.zutubi.bundles.producer_2.0.0.jar");
        producer3 = new File(bundleDir, "com.zutubi.bundles.producer_3.0.0.jar");

        objectFactory = new DefaultObjectFactory();
    }

    @AfterMethod
    protected void tearDown() throws Exception
    {
        try
        {
            shutdownPluginCore();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        objectFactory = null;

        if (!FileSystemUtils.rmdir(tmpDir))
        {
            // deletion consistently fails on windows - an open file handle to the bundle jars being the cause.
        }

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

        // restart plugin core
        restartPluginCore();

        assertFalse(pluginUpgradeManager.isUpgradeRequired());
    }

    public void testRequestUpgradeViaPluginInterface() throws Exception
    {
        FileSystemUtils.copy(paths.getPluginStorageDir(), producer2);

        startupPluginCore();

        assertFalse(pluginUpgradeManager.isUpgradeRequired());

        Plugin plugin = pluginManager.getPlugin("com.zutubi.bundles.producer");
        plugin.upgrade(producer3.toURI());
        assertEquals(Plugin.State.UPDATING, plugin.getState());

        restartPluginCore();

        assertTrue(pluginUpgradeManager.isUpgradeRequired());

        plugin = pluginManager.getPlugin("com.zutubi.bundles.producer");
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

        assertFalse(pluginManager.isVersionChangeDetected());
        assertFalse(pluginUpgradeManager.isUpgradeRequired());

        Plugin plugin = pluginManager.getInternalPlugin("com.zutubi.bundles.producer");
        if (plugin == null)
        {
            plugin = pluginManager.getPlugin("com.zutubi.bundles.producer");
        }
        
        assertEquals(Plugin.State.ENABLED, plugin.getState());
    }

    private void assertNoUpgradeHandledCorrectly(File dir, boolean internal) throws Exception
    {
        FileSystemUtils.copy(dir, producer1);

        startupPluginCore();

        assertFalse(pluginUpgradeManager.isUpgradeRequired());

        shutdownPluginCore();

        FileSystemUtils.delete(new File(dir, producer1.getName()));
        FileSystemUtils.copy(dir, producer2);

        startupPluginCore();

        if (!internal)
        {
            assertTrue(pluginManager.isVersionChangeDetected());
        }

        assertFalse(pluginUpgradeManager.isUpgradeRequired());

        Plugin plugin = pluginManager.getInternalPlugin("com.zutubi.bundles.producer");
        if (plugin == null)
        {
            plugin = pluginManager.getPlugin("com.zutubi.bundles.producer");
        }
        assertEquals(Plugin.State.ENABLED, plugin.getState());
    }

    private void assertUpgradeHandledCorrectly(File dir, boolean internal) throws Exception
    {
        FileSystemUtils.copy(dir, producer2);

        startupPluginCore();

        assertEquals("2.0.0", pluginManager.getPluginRegistry().getEntry("com.zutubi.bundles.producer").get(PluginManager.PLUGIN_VERSION_KEY));

        shutdownPluginCore();

        FileSystemUtils.delete(new File(dir, producer2.getName()));
        FileSystemUtils.copy(dir, producer3);

        startupPluginCore();
        
        if (!internal)
        {
            assertTrue(pluginManager.isVersionChangeDetected());
        }

        assertTrue(pluginUpgradeManager.isUpgradeRequired());
        assertEquals("2.0.0", pluginManager.getPluginRegistry().getEntry("com.zutubi.bundles.producer").get(PluginManager.PLUGIN_VERSION_KEY));
    }

    private void restartPluginCore() throws Exception
    {
        shutdownPluginCore();
        startupPluginCore();
    }

    private void startupPluginCore() throws PluginException
    {
        try
        {
            pluginManager = new PluginManager();
            pluginManager.setPluginPaths(paths);
            pluginManager.init();

            pluginUpgradeManager = new PluginUpgradeManager();
            pluginUpgradeManager.setPluginManager(pluginManager);
            pluginUpgradeManager.setObjectFactory(objectFactory);
            pluginUpgradeManager.init();
        }
        catch (Exception e)
        {
            throw new PluginException(e);
        }
    }

    protected void shutdownPluginCore() throws Exception
    {
        if (pluginManager != null)
        {
            pluginManager.destroy();
            pluginManager = null;
            OSGIUtilsAccessor.reset();
        }
        pluginUpgradeManager = null;
    }

    public static class OSGIUtilsAccessor
    {
        public static void reset() throws IllegalAccessException, NoSuchFieldException
        {
            Field f = OSGIUtils.class.getDeclaredField("singleton");
            f.setAccessible(true);
            f.set(OSGIUtils.getDefault(), null);
        }

        public static void nullifyFields() throws NoSuchFieldException, IllegalAccessException
        {
            String[] fieldNames = new String[]{"bundleTracker", "debugTracker", "configurationLocationTracker"};

            for (String fieldName : fieldNames)
            {
                Field f = OSGIUtils.class.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(OSGIUtils.getDefault(), null);
            }
        }

        public static void initServices() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
        {
            Method m = OSGIUtils.class.getDeclaredMethod("initServices");
            m.setAccessible(true);
            m.invoke(OSGIUtils.getDefault());
        }

        public static void closeServices() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
        {
            Method m = OSGIUtils.class.getDeclaredMethod("closeServices");
            m.setAccessible(true);
            m.invoke(OSGIUtils.getDefault());
        }
    }

}

