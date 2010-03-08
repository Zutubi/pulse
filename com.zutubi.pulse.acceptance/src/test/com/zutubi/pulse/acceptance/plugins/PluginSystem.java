package com.zutubi.pulse.acceptance.plugins;

import com.zutubi.pulse.acceptance.support.JythonPulseTestFactory;
import com.zutubi.pulse.acceptance.support.Pulse;
import com.zutubi.pulse.acceptance.support.PulsePackage;
import com.zutubi.pulse.acceptance.support.PulseTestFactory;
import com.zutubi.pulse.core.plugins.*;
import org.eclipse.core.internal.registry.osgi.OSGIUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 *
 */
public class PluginSystem
{
    private ConfigurablePluginPaths paths;
    private PluginManager manager;

    public PluginSystem(File pkgFile, File tmpDir) throws Exception
    {
        PulseTestFactory factory = new JythonPulseTestFactory();
        PulsePackage pkg = factory.createPackage(pkgFile);

        Pulse pulse = pkg.extractTo(tmpDir.getCanonicalPath());
        File pluginRoot = new File(pulse.getPluginRoot());

        File internalDir = new File(pluginRoot, "internal");
        File prepackagedDir = new File(pluginRoot, "prepackaged");
        File osgiDir = new File(pluginRoot, "config");

        File storageDir = new File(tmpDir, "plugin/storage");

        File workDir = new File(tmpDir, "plugin/work");

        paths = new ConfigurablePluginPaths();
        paths.setPrepackagedPluginStorageDir(prepackagedDir);
        paths.setInternalPluginStorageDir(internalDir);
        paths.setOsgiConfigurationDir(osgiDir);
        paths.setPluginRegistryDir(storageDir);
        paths.setPluginStorageDir(storageDir);
        paths.setPluginWorkDir(workDir);
    }

    public Plugin install(File file) throws PluginException
    {
        return getPluginManager().install(file.toURI());
    }

    public PluginManager getPluginManager()
    {
        return manager;
    }

    public PluginPaths getPluginPaths()
    {
        return paths;
    }

    /**
     * Startup the pulse plugin system.
     */
    public void startup() throws Exception
    {
        if (!isStarted())
        {
            manager = new PluginManager();
            manager.setPluginPaths(paths);
            manager.init();
        }
    }

    /**
     * Shutdown the pulse plugin system.
     */
    public void shutdown() throws Exception
    {
        if (!isShutdown())
        {
            manager.destroy();
            manager = null;
            OSGIUtilsAccessor.reset();
        }
    }

    public boolean isStarted()
    {
        return manager != null;
    }

    public boolean isShutdown()
    {
        return manager == null;
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
