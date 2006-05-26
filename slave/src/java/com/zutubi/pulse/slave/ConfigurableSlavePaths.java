package com.zutubi.pulse.slave;

import java.io.File;

/**
 */
public class ConfigurableSlavePaths implements SlavePaths
{
    private File systemRoot;
    private File contentRoot;
    private File configRoot;

    public File getSystemRoot()
    {
        return systemRoot;
    }

    public void setSystemRoot(File systemRoot)
    {
        this.systemRoot = systemRoot;
    }

    public File getContentRoot()
    {
        return contentRoot;
    }

    public void setContentRoot(File contentRoot)
    {
        this.contentRoot = contentRoot;
    }

    public File getConfigRoot()
    {
        return configRoot;
    }

    public void setConfigRoot(File configRoot)
    {
        this.configRoot = configRoot;
    }
}
