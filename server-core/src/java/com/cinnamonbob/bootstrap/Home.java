package com.cinnamonbob.bootstrap;

import com.cinnamonbob.Version;

import java.io.File;
import java.io.IOException;

/**
 * The home object provides an interface to the configured home directory,
 * its layout and its data.
 *
 * The home directory is layed out as follows:
 *
 *      home/
 *          config/: user configuration files
 *          database/: the embedded HSQL database
 *          projects/: build artifacts
 *
 *          bob.config.properties: core configuration properties, contain version and license details amongst other things
 *
 */
public class Home implements UserPaths
{
    private final File bobHome;

    private Version homeVersion;
    private File userConfigRoot;
    private File projectRoot;
    private File databaseRoot;
    private static final String CONFIG_FILE_NAME = "bob.config.properties";

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
        return new File(bobHome, CONFIG_FILE_NAME);
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
