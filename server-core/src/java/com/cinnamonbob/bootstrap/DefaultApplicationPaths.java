package com.cinnamonbob.bootstrap;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

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
    private List<File> templateRoots;
    private File userConfigRoot;
    private File databaseRoot;
    private File projectRoot;

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

    public List<File> getTemplateRoots()
    {
        if (templateRoots == null)
        {
            templateRoots = new LinkedList<File>();
            templateRoots.add(new File(getSystemRoot(), "templates"));
            templateRoots.add(new File(getSystemRoot(), "www"));
        }
        return templateRoots;
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

    public File getProjectRoot()
    {
        if (projectRoot == null)
        {
            projectRoot = new File(getSystemRoot(), "projects");
        }
        return projectRoot;
    }
}
