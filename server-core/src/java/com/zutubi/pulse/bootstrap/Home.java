/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.Version;

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
 *          pulse.config.properties: core configuration properties, contain version and license details amongst other things
 *
 */
public class Home implements UserPaths
{
    private final File pulseHome;

    private Version homeVersion;
    private File userConfigRoot;
    private File projectRoot;
    private File databaseRoot;
    public static final String CONFIG_FILE_NAME = "pulse.config.properties";

    public Home(File homeDir)
    {
        this.pulseHome = homeDir;
    }

    public boolean isInitialised()
    {
        if (!pulseHome.exists())
        {
            return false;
        }

        return getVersionFile().exists();
    }

    public void init() throws IOException
    {
        // create the home directory.
        if (!pulseHome.exists() && !pulseHome.mkdirs())
        {
            throw new StartupException("Failed to create the configured home directory: "+pulseHome +".");
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
        return new File(pulseHome, CONFIG_FILE_NAME);
    }

    public File getHome()
    {
        return pulseHome;
    }

    public File getUserConfigRoot()
    {
        if (userConfigRoot == null)
        {
            userConfigRoot = new File(pulseHome, "config");
        }
        return userConfigRoot;
    }

    public File getDatabaseRoot()
    {
        if (databaseRoot == null)
        {
            databaseRoot = new File(pulseHome, "database");
        }
        return databaseRoot;
    }

    public File getProjectRoot()
    {
        if (projectRoot == null)
        {
            projectRoot = new File(pulseHome, "projects");
        }
        return projectRoot;
    }
}
