package com.cinnamonbob.bootstrap;

import java.io.File;

/**
 * <class-comment/>
 */
public class ConfigurableUserPaths implements UserPaths
{
    private File home;
    private File databaseRoot;
    private File projectRoot;
    private File userConfigRoot;

    public File getHome()
    {
        return home;
    }

    public void setHome(File home)
    {
        this.home = home;
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

    public File getUserConfigRoot()
    {
        return userConfigRoot;
    }

    public void setUserConfigRoot(File userConfigRoot)
    {
        this.userConfigRoot = userConfigRoot;
    }

}
