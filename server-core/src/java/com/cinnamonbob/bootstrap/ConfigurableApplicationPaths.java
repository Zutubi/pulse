package com.cinnamonbob.bootstrap;

import java.io.File;
import java.util.List;

/**
 * 
 *
 */
public class ConfigurableApplicationPaths implements ApplicationPaths
{
    private File systemRoot;
    private File contentRoot;
    private File configRoot;
    private List<File> templateRoots;
    private File userConfigRoot;
    private File databaseRoot;
    private File projectRoot;

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

    public File getUserConfigRoot()
    {
        return userConfigRoot;
    }

    public List<File> getTemplateRoots()
    {
        return templateRoots;
    }

    public void setTemplateRoots(List<File> templateRoots)
    {
        this.templateRoots = templateRoots;
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


    public File getProjectRoot()
    {
        return projectRoot;
    }

    public void setProjectRoot(File projectRoot)
    {
        this.projectRoot = projectRoot;
    }
}
