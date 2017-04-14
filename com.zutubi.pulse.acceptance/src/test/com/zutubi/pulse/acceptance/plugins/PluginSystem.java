/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.acceptance.plugins;

import com.zutubi.pulse.acceptance.support.Pulse;
import com.zutubi.pulse.acceptance.support.PulsePackage;
import com.zutubi.pulse.acceptance.support.PulseTestFactory;
import com.zutubi.pulse.acceptance.support.jython.JythonPulseTestFactory;
import com.zutubi.pulse.core.plugins.*;
import com.zutubi.util.io.FileSystemUtils;
import org.eclipse.core.internal.registry.osgi.OSGIUtils;

import java.io.File;
import java.io.IOException;
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
        if (!storageDir.exists() && !storageDir.mkdirs())
        {
            throw new IOException("Cannot create plugin storage directory '" + storageDir.getAbsolutePath() + "'");
        }
        
        File workDir = new File(tmpDir, "plugin/work");

        // Remove the jobs plugin as it is not possible to restart within a
        // process, and we don't need it for these tests.
        removeJobsPlugin(internalDir);
        
        paths = new ConfigurablePluginPaths();
        paths.setPrepackagedPluginStorageDir(prepackagedDir);
        paths.setInternalPluginStorageDir(internalDir);
        paths.setOsgiConfigurationDir(osgiDir);
        paths.setPluginRegistryDir(storageDir);
        paths.setPluginStorageDir(storageDir);
        paths.setPluginWorkDir(workDir);
    }

    private void removeJobsPlugin(File internalDir)
    {
        File jobsPlugin = FileSystemUtils.findFirstChildMatching(internalDir, ".*jobs.*");
        if (!jobsPlugin.delete())
        {
            throw new RuntimeException("Unable to remove that troublesome jobs plugin");
        }
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
