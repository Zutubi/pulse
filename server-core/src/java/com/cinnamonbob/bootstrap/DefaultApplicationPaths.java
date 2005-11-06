package com.cinnamonbob.bootstrap;

import java.io.File;

/**
 * 
 *
 */
public class DefaultApplicationPaths implements ApplicationPaths
{
    private final File bobHome;

    private File contentRoot;
    private File configRoot;
    private File templateRoot;
    private File userConfigRoot;
    private File databaseRoot;
    
    public DefaultApplicationPaths(File bobHome)
    {
        this.bobHome = bobHome;
    }

    public File getApplicationRoot()
    {
        return bobHome;
    }

    public File getDatabaseRoot()
    {
        if (databaseRoot == null)
        {
             databaseRoot = new File(bobHome, "system" + File.separatorChar + "database");   
        }
        return databaseRoot;
    }

    public File getContentRoot()
    {
        if (contentRoot == null)
        {
            contentRoot = new File(bobHome, "system" + File.separatorChar + "www");
        }
        return contentRoot;
    }

    public File getConfigRoot()
    {
        if (configRoot == null)
        {
            configRoot = new File(bobHome, "system" + File.separatorChar + "config");
        }
        return configRoot;
    }

    public File getTemplateRoot()
    {
        if (templateRoot == null)
        {
            templateRoot = new File(bobHome, "system" + File.separatorChar + "templates");
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
}
