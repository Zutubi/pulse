package com.cinnamonbob.bootstrap;

import java.io.File;

/**
 * 
 *
 */
public class DefaultApplicationPaths implements ApplicationPaths
{
    private final File bobHome;

    private File systemRoot;
    private File contentRoot;
    private File configRoot;
    private File templateRoot;
    private File userConfigRoot;
    private File databaseRoot;

    public DefaultApplicationPaths(File bobHome)
    {
        this.bobHome = bobHome;
    }

    public File getSystemRoot()
    {
        if (systemRoot == null)
        {
            systemRoot = new File(bobHome, "system");
        }
        return systemRoot;
    }

    public File getContentRoot()
    {
        if (contentRoot == null)
        {
            contentRoot = new File(getSystemRoot(), "www");
        }
        return contentRoot;
    }

    public File getConfigRoot()
    {
        if (configRoot == null)
        {
            configRoot = new File(getSystemRoot(), "config");
        }
        return configRoot;
    }

    public File getTemplateRoot()
    {
        if (templateRoot == null)
        {
            templateRoot = new File(getSystemRoot(), "templates");
        }
        return templateRoot;
    }

    public File getUserConfigRoot()
    {
        if (userConfigRoot == null)
        {
            userConfigRoot = new File(bobHome, "config");
        }
        return userConfigRoot;
    }

    public File getDatabaseRoot()
    {
        if (databaseRoot == null)
        {
            databaseRoot = new File(getSystemRoot(), "database");
        }
        return databaseRoot;
    }
}
