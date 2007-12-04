package com.zutubi.pulse.dev.bootstrap;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.command.PulseCtl;

import java.io.File;

/**
 * Default implementation of DevPaths: used in a real Pulse instance.
 */
public class DefaultDevPaths implements DevPaths
{
    private File versionHome;
    private File userRoot;
    private File systemPluginRoot;
    private File pluginConfigurationRoot;
    private File internalPluginRoot;
    private File prepackagedPluginRoot;
    private File userPluginRoot;

    public DefaultDevPaths()
    {
        String v = System.getProperty(PulseCtl.VERSION_HOME);
        versionHome = new File(v);
    }

    public File getUserRoot()
    {
        if (userRoot == null)
        {
            File homeDir;
            String userHome = System.getProperty("user.home");
            if(userHome == null)
            {
                homeDir = new File(".");
            }
            else
            {
                homeDir = new File(userHome);
            }

            userRoot = new File(homeDir, ".pulse-dev");
        }

        return userRoot;
    }

    private File getSystemPluginRoot()
    {
        if(systemPluginRoot == null)
        {
            systemPluginRoot = new File(versionHome, FileSystemUtils.composeFilename("system", "plugins"));
        }
        return systemPluginRoot;
    }

    public File getOsgiConfigurationDir()
    {
        if(pluginConfigurationRoot == null)
        {
            pluginConfigurationRoot = new File(getSystemPluginRoot(), "config");
        }
        return pluginConfigurationRoot;
    }

    public File getInternalPluginStorageDir()
    {
        if(internalPluginRoot == null)
        {
            internalPluginRoot = new File(getSystemPluginRoot(), "internal");
        }
        return internalPluginRoot;
    }

    public File getPrepackagedPluginStorageDir()
    {
        if(prepackagedPluginRoot == null)
        {
            prepackagedPluginRoot = new File(getSystemPluginRoot(), "prepackaged");
        }
        return prepackagedPluginRoot;
    }

    public File getPluginStorageDir()
    {
        if(userPluginRoot == null)
        {
            userPluginRoot = new File(getUserRoot(), "plugins");
        }
        return userPluginRoot;
    }

    public File getPluginRegistryDir()
    {
        return getPluginStorageDir();
    }

    public File getPluginWorkDir()
    {
        // not sure where this should point? just need some scratch space. Any temp directory would suit.
        return null;
    }
}
