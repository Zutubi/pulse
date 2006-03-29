package com.cinnamonbob.bootstrap;

import com.cinnamonbob.Version;

import java.io.File;
import java.io.IOException;

/**
 * The home object provides an interface to the configured home directory,
 * its layout and its data.
 */
public class Home implements UserPaths
{
    private final File bobHome;

    private Version homeVersion;
    private File userConfigRoot;
    private File projectRoot;
    private File databaseRoot;

    public Home(File homeDir)
    {
        this.bobHome = homeDir;
    }

    public boolean isInitialised()
    {
        if (!bobHome.exists())
        {
            return false;
        }

        return getVersionFile().exists();
    }

    public void init() throws IOException
    {
        // create the home directory.
        if (!bobHome.exists() && !bobHome.mkdirs())
        {
            throw new StartupException("Failed to create the configured home directory: "+bobHome +".");
        }

        // write the version file.
        Version systemVersion = Version.getVersion();
        updateVersion(systemVersion);
    }

    public void updateVersion(Version version) throws IOException
    {
        version.write(getVersionFile());
    }

    public Version getHomeVersion()
    {
        if (homeVersion == null)
        {
            try
            {
                homeVersion = Version.load(getVersionFile());
            }
            catch (IOException e)
            {
                //noop.
            }
        }
        return homeVersion;
    }

    private File getVersionFile()
    {
        return new File(bobHome, "version.properties");
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
