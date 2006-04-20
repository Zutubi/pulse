/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseDecoder;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;
import com.opensymphony.util.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * The home object provides an interface to the configured home directory,
 * its layout and its data.
 * <p/>
 * The home directory is layed out as follows:
 * <p/>
 * home/
 * config/: user configuration files
 * database/: the embedded HSQL database
 * projects/: build artifacts
 * <p/>
 * pulse.config.properties: core configuration properties, contain version and license details amongst other things
 */
public class Home implements UserPaths
{
    private static final Logger LOG = Logger.getLogger(Home.class);

    private final File pulseHome;

    private Version homeVersion;
    private File userConfigRoot;
    private File projectRoot;
    private File databaseRoot;

    public static final String CONFIG_FILE_NAME = "pulse.config.properties";

    private Properties config = null;
    private String licenseKey;
    private License license;

    protected Home(File homeDir)
    {
        this.pulseHome = homeDir;
    }

    /**
     * The home directory MUST be initialised before the application can start.
     *
     * @return true if the home is initialised
     * @see #init()
     */
    public boolean isInitialised()
    {
        if (!pulseHome.exists())
        {
            return false;
        }

        return getConfigFile().exists();
    }

    /**
     * Initialise the home directory. This will ensure that the necessary directories
     * and configuration files are setup.
     *
     * @throws IOException
     */
    public void init() throws IOException
    {
        if (isInitialised())
        {
            throw new StartupException("Can not initialise a home directory that is already initialised.");
        }

        // create the home directory.
        if (!pulseHome.exists() && !pulseHome.mkdirs())
        {
            throw new StartupException("Failed to create the configured home directory: " + pulseHome + ".");
        }

        // write the version file.
        Version systemVersion = Version.getVersion();
        updateVersion(systemVersion);
    }

    /**
     * Update the version recorded in the home directory.
     *
     * @param version
     * @throws IOException
     */
    public void updateVersion(Version version) throws IOException
    {
        version.write(getConfig());
        IOUtils.write(getConfig(), getConfigFile());
    }

    /**
     * Retrieve the version recorded in the home directory.
     *
     * @return Version
     */
    public Version getVersion()
    {
        if (homeVersion == null)
        {
            try
            {
                homeVersion = Version.read(getConfig());
            }
            catch (IOException e)
            {
                LOG.severe("Failed to load the config. Cause: " + e.getMessage(), e);
            }
        }
        return homeVersion;
    }

    public void updateLicenseKey(String key) throws IOException
    {
        getConfig().setProperty("license.key", key);
        IOUtils.write(getConfig(), getConfigFile());
    }

    public String getLicenseKey() throws IOException
    {
        if (licenseKey == null)
        {
            licenseKey = getConfig().getProperty("license.key");
        }
        return licenseKey;
    }

    public License getLicense()
    {
        if (license == null)
        {
            try
            {
                String licenseKey = getLicenseKey();
                if (TextUtils.stringSet(licenseKey))
                {
                    LicenseDecoder decoder = new LicenseDecoder();
                    license = decoder.decode(licenseKey.getBytes());
                }
            }
            catch (LicenseException e)
            {
                LOG.severe("Failed to decode the license.", e);
                return null;
            }
            catch (IOException e)
            {
                LOG.severe("Failed to retrieve the license key.", e);
                return null;
            }
        }
        return license;
    }

    /**
     * Retrieve the home directory.
     *
     * @return the configured home root directory.
     */
    public File getHome()
    {
        return pulseHome;
    }

    /**
     * @see com.zutubi.pulse.bootstrap.UserPaths#getUserConfigRoot()
     */
    public File getUserConfigRoot()
    {
        if (userConfigRoot == null)
        {
            userConfigRoot = new File(pulseHome, "config");
        }
        return userConfigRoot;
    }

    /**
     * @see com.zutubi.pulse.bootstrap.UserPaths#getDatabaseRoot()
     */
    public File getDatabaseRoot()
    {
        if (databaseRoot == null)
        {
            databaseRoot = new File(pulseHome, "database");
        }
        return databaseRoot;
    }

    /**
     * @see com.zutubi.pulse.bootstrap.UserPaths#getProjectRoot()
     */
    public File getProjectRoot()
    {
        if (projectRoot == null)
        {
            projectRoot = new File(pulseHome, "projects");
        }
        return projectRoot;
    }

    private Properties getConfig() throws IOException
    {
        if (config == null)
        {
            if (getConfigFile().exists())
            {
                config = IOUtils.read(getConfigFile());
            }
            else
            {
                config = new Properties();
            }
        }
        return config;
    }

    private File getConfigFile()
    {
        return new File(pulseHome, CONFIG_FILE_NAME);
    }
}
