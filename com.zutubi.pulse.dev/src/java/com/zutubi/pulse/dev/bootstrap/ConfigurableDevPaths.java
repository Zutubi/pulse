package com.zutubi.pulse.dev.bootstrap;

import com.zutubi.pulse.plugins.ConfigurablePluginPaths;

import java.io.File;

/**
 * An implementation of DevPaths that may be configured with any combination
 * of paths, used during development/testing.
 */
public class ConfigurableDevPaths extends ConfigurablePluginPaths implements DevPaths
{
    private File userRoot;

    public File getUserRoot()
    {
        return userRoot;
    }

    public void setUserRootString(String userRoot)
    {
        this.userRoot = new File(userRoot);
    }
}
