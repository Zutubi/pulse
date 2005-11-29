package com.cinnamonbob.bootstrap;

import java.io.File;

/**
 * 
 *
 */
public class ConfigurableApplicationPaths implements ApplicationPaths
{
    private File systemRoot;
    private File contentRoot;
    private File configRoot;
    private File templateRoot;
    private File userConfigRoot;
    private File databaseRoot;

    public File getSystemRoot()
    {
        return systemRoot;
    }

    public File getContentRoot()
    {
        return contentRoot;
    }

    public File getConfigRoot()
    {
        return configRoot;
    }

    public File getTemplateRoot()
    {
        return templateRoot;
    }

    public File getUserConfigRoot()
    {
        return userConfigRoot;
    }

    public void setSystemRoot(File systemRoot)
    {
        this.systemRoot = systemRoot;
    }

    public void setContentRoot(File contentRoot)
    {
        this.contentRoot = contentRoot;
    }

    public void setConfigRoot(File configRoot)
    {
        this.configRoot = configRoot;
    }

    public void setTemplateRoot(File templateRoot)
    {
        this.templateRoot = templateRoot;
    }

    public void setUserConfigRoot(File userConfigRoot)
    {
        this.userConfigRoot = userConfigRoot;
    }

    public File getDatabaseRoot()
    {
        return databaseRoot;
    }

    public void setDatabaseRoot(File databaseRoot)
    {
        this.databaseRoot = databaseRoot;
    }
}
