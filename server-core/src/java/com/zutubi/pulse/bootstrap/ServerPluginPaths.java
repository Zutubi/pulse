package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.plugins.PluginPaths;

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

    private File getSystemPluginRoot()
    {
        if(systemPluginRoot == null)
        {
            systemPluginRoot = new File(configurationManager.getSystemPaths().getSystemRoot(), "plugins");
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
            userPluginRoot = new File(configurationManager.getUserPaths().getData(), "plugins");
        }
        return userPluginRoot;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
