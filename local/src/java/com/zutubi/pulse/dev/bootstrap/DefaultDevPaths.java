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

    public File getPluginConfigurationRoot()
    {
        if(pluginConfigurationRoot == null)
        {
            pluginConfigurationRoot = new File(getSystemPluginRoot(), "config");
        }
        return pluginConfigurationRoot;
    }

    public File getInternalPluginRoot()
    {
        if(internalPluginRoot == null)
        {
            internalPluginRoot = new File(getSystemPluginRoot(), "internal");
        }
        return internalPluginRoot;
    }

    public File getPrepackagedPluginRoot()
    {
        if(prepackagedPluginRoot == null)
        {
            prepackagedPluginRoot = new File(getSystemPluginRoot(), "prepackaged");
        }
        return prepackagedPluginRoot;
    }

    public File getUserPluginRoot()
    {
        if(userPluginRoot == null)
        {
            userPluginRoot = new File(getUserRoot(), "plugins");
        }
        return userPluginRoot;
    }
}
