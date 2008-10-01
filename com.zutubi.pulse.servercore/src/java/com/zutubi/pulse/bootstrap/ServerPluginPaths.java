package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.core.plugins.PluginPaths;

import java.io.File;

/**
 * An implementation of the plugin paths shared by master and slave.
 */
public class ServerPluginPaths implements PluginPaths
{
    private ConfigurationManager configurationManager;
    private File systemPluginRoot;
    private File pluginConfigurationRoot;
    private File internalPluginRoot;
    private File prepackagedPluginRoot;
    private File userPluginRoot;

    public void init()
    {
    }

    private File getSystemPluginRoot()
    {
        if(systemPluginRoot == null)
        {
            systemPluginRoot = new File(configurationManager.getSystemPaths().getSystemRoot(), "plugins");
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
            UserPaths userPaths = configurationManager.getUserPaths();
            if (userPaths != null)
            {
                File dataDir = userPaths.getData();
                if (dataDir != null)
                {
                    userPluginRoot = new File(dataDir, "plugins");
                }
            }
        }
        return userPluginRoot;
    }

    public File getPluginRegistryDir()
    {
        return getPluginStorageDir();
    }

    public File getPluginWorkDir()
    {
        return configurationManager.getSystemPaths().getTmpRoot();
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
