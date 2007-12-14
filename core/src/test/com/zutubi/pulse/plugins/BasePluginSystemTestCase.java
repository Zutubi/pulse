package com.zutubi.pulse.plugins;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

import org.eclipse.core.internal.registry.osgi.OSGIUtils;

/**
 *
 *
 */
public abstract class BasePluginSystemTestCase extends PulseTestCase
{
    private File tmpDir;
    
    protected ConfigurablePluginPaths paths;
    protected PluginManager manager;

    protected void setUp() throws Exception
    {
        super.setUp();

        // base directory will be cleaned up at the end of the test.
        tmpDir = FileSystemUtils.createTempDir(PluginManagerTest.class.getName(), "");

        File internalDir = new File(tmpDir, "internal");
        assertTrue(internalDir.mkdirs());
        File prepackagedDir = new File(tmpDir, "prepackaged");
        assertTrue(prepackagedDir.mkdirs());

        File storageDir = new File(tmpDir, "storage");
        assertTrue(storageDir.mkdirs());
        File configDir = new File(tmpDir, "config");
        assertTrue(configDir.mkdirs());
        File workDir = new File(tmpDir, "work");
        assertTrue(workDir.mkdirs());

        File osgiDir = new File(tmpDir, "osgi");
        assertTrue(osgiDir.mkdirs());

        paths = new ConfigurablePluginPaths();
        paths.setPrepackagedPluginStorageDir(prepackagedDir);
        paths.setInternalPluginStorageDir(internalDir);
        paths.setOsgiConfigurationDir(osgiDir);
        paths.setPluginRegistryDir(configDir);
        paths.setPluginStorageDir(storageDir);
        paths.setPluginWorkDir(workDir);

        // copy contents from etc osgi into temp osgi dir.
        FileSystemUtils.copy(osgiDir, new File(getPulseRoot(), "master/etc/osgi").listFiles(new FileFilter()
        {
            public boolean accept(File file)
            {
                return !file.isDirectory();
            }
        }));
    }

    protected void tearDown() throws Exception
    {
        shutdownPluginCore();

        paths = null;
        manager = null;

        removeDirectory(tmpDir);

        super.tearDown();
    }

    protected void installPulseInternalBundles() throws IOException
    {
        String internalPath = FileSystemUtils.composeFilename("plugins", "internal");
        FileSystemUtils.copy(paths.getInternalPluginStorageDir(), new File(getPulseRoot(), internalPath));
    }

    protected void restartPluginCore() throws Exception
    {
        shutdownPluginCore();
        startupPluginCore();
    }

    protected void startupPluginCore() throws PluginException
    {
        try
        {
            manager = new PluginManager();
            manager.setPluginPaths(paths);
            manager.init();
        }
        catch (Exception e)
        {
            throw new PluginException(e);
        }
    }

    protected void shutdownPluginCore() throws Exception
    {
        if (manager != null)
        {
            try
            {
                manager.destroy();
                OSGIUtilsAccessor.reset();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
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
