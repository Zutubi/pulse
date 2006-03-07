package com.cinnamonbob.bootstrap;

import java.io.File;

/**
 * <class-comment/>
 */
public class DefaultUserPaths implements UserPaths
{
    private final File bobHome;

    private File userConfigRoot;
    private File databaseRoot;
    private File projectRoot;

    public DefaultUserPaths(File bobHome)
    {
        this.bobHome = bobHome;
    }

    public File getHome()
    {
        return bobHome;
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
            databaseRoot = new File(bobHome, "database");
        }
        return databaseRoot;
    }

    public File getProjectRoot()
    {
        if (projectRoot == null)
        {
            projectRoot = new File(bobHome, "projects");
        }
        return projectRoot;
    }
}
