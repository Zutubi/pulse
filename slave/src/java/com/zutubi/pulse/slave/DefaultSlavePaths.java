package com.zutubi.pulse.slave;

import java.io.File;

/**
 */
public class DefaultSlavePaths implements SlavePaths
{
    private File pulseHome;
    private File systemRoot;
    private File contentRoot;
    private File systemConfigRoot;

    public DefaultSlavePaths(File pulseHome)
    {
        this.pulseHome = pulseHome;
    }

    public File getSystemRoot()
    {
        if(systemRoot == null)
        {
            systemRoot = new File(pulseHome, "system");
        }

        return systemRoot;
    }

    public File getContentRoot()
    {
        if(contentRoot == null)
        {
            contentRoot = new File(getSystemRoot(), "www");
        }

        return contentRoot;
    }

    public File getConfigRoot()
    {
        if(systemConfigRoot == null)
        {
            systemConfigRoot = new File(getSystemRoot(), "config");
        }

        return systemConfigRoot;
    }
}
