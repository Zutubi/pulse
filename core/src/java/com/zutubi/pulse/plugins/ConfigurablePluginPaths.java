package com.zutubi.pulse.plugins;

import java.io.File;

/**
 * An implementation of PluginPaths that is fully configurable: used to set
 * up paths for within a development environment.
 */
public class ConfigurablePluginPaths implements PluginPaths
{
    private File pluginConfigurationRoot;
    private File internalPluginRoot;
    private File prepackagedPluginRoot;
    private File userPluginRoot;

    public File getPluginConfigurationRoot()
    {
        return pluginConfigurationRoot;
    }

    public void setPluginConfigurationRootString(String pluginConfigurationRoot)
    {
        this.pluginConfigurationRoot = new File(pluginConfigurationRoot);
    }

    public File getInternalPluginRoot()
    {
        return internalPluginRoot;
    }

    public void setInternalPluginRootString(String internalPluginRoot)
    {
        this.internalPluginRoot = new File(internalPluginRoot);
    }

    public File getPrepackagedPluginRoot()
    {
        return prepackagedPluginRoot;
    }

    public void setPrepackagedPluginRootString(String prepackagedPluginRoot)
    {
        this.prepackagedPluginRoot = new File(prepackagedPluginRoot);
    }

    public File getUserPluginRoot()
    {
        return userPluginRoot;
    }

    public void setUserPluginRootString(String userPluginRoot)
    {
        this.userPluginRoot = new File(userPluginRoot);
    }
}
