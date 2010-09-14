package com.zutubi.pulse.dev.bootstrap;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.command.PulseCtl;

import java.io.File;

/**
 * Default implementation of DevPaths: used in a real Pulse instance.
 */
public class DefaultDevPaths implements DevPaths
{
    private File versionHome;
    private File userRoot;
    private File systemRoot;
    private File systemPluginRoot;
    private File pluginConfigurationRoot;
    private File internalPluginRoot;
    private File prepackagedPluginRoot;
    private File userPluginRoot;
    private File pluginWorkRoot;

    public DefaultDevPaths()
    {
        String v = System.getProperty(PulseCtl.VERSION_HOME);
        versionHome = new File(v);
    }

    public DefaultDevPaths(File userRoot, File versionHome)
    {
        this.userRoot = userRoot;
        this.versionHome = versionHome;
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

            userRoot = new File(homeDir, ".pulse" + getMajorMinor() + "-dev");
        }

        return userRoot;
    }

    private String getMajorMinor()
    {
        String versionNumber = Version.getVersion().getVersionNumber();
        String[] pieces = versionNumber.split("\\.");
        if (pieces.length == 1)
        {
            return pieces[0];
        }
        else
        {
            return pieces[0] + pieces[1];
        }
    }

    private File getSystemRoot()
    {
        if(systemRoot == null)
        {
            systemRoot = new File(versionHome, "system");
        }
        return systemRoot;
    }

    private File getSystemPluginRoot()
    {
        if(systemPluginRoot == null)
        {
            systemPluginRoot = new File(getSystemRoot(), "plugins");
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
        if(pluginWorkRoot == null)
        {
            pluginWorkRoot = new File(getSystemRoot(), "tmp");
            if(!pluginWorkRoot.isDirectory())
            {
                pluginWorkRoot.mkdirs();
            }
        }

        return pluginWorkRoot;
    }
}
