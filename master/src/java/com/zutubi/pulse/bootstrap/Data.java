package com.zutubi.pulse.bootstrap;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.config.Config;
import com.zutubi.pulse.config.FileConfig;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseDecoder;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;
import java.util.Properties;

/**
 * The data object provides an interface to the configured data directory,
 * its layout and its data.
 * <p/>
 * The data directory is layed out as follows:
 * <p/>
 * data/
 * config/: user configuration files
 * database/: the embedded HSQL database
 * projects/: build artifacts
 * <p/>
 * pulse.config.properties: core configuration properties, contain version and license details amongst other things
 */
public class Data implements MasterUserPaths
{
    private static final Logger LOG = Logger.getLogger(Data.class);

    private final File pulseData;

    private Version dataVersion;
    private File userConfigRoot;
    private File projectRoot;
    private File userRoot;
    private File databaseRoot;

    public static final String CONFIG_FILE_NAME = "pulse.config.properties";

    private Config config = null;
    private String licenseKey;
    private License license;
    private static final String LICENSE_KEY = "license.key";

    public Data(File dataDir)
    {
        this.pulseData = dataDir;
    }

    /**
     * The data directory MUST be initialised before the application can start.
     *
     * @return true if the data is initialised
     * @see #init()
     */
    public boolean isInitialised()
    {
        if (!pulseData.exists())
        {
            return false;
        }

        return getConfigFile().exists();
    }

    /**
     * Initialise the data directory. This will ensure that the necessary directories
     * and configuration files are setup.
     *
     */
    public void init()
    {
        if (isInitialised())
        {
            throw new StartupException("Can not initialise a data directory that is already initialised.");
        }

        // create the data directory.
        if (!pulseData.exists() && !pulseData.mkdirs())
        {
            throw new StartupException("Failed to create the configured data directory: " + pulseData + ".");
        }

        // write the version file.
        Version systemVersion = Version.getVersion();
        updateVersion(systemVersion);
    }

    /**
     * Update the version recorded in the data directory.
     *
     * @param version
     */
    public void updateVersion(Version version)
    {
        // we are messing around writing to a properties object first because the Config interface is
        // not available in the pulse core.
        getConfig().setProperty(Version.BUILD_DATE, version.getBuildDate());
        getConfig().setProperty(Version.BUILD_NUMBER, version.getBuildNumber());
        getConfig().setProperty(Version.RELEASE_DATE, version.getReleaseDate());
        getConfig().setProperty(Version.VERSION_NUMBER, version.getVersionNumber());
    }

    public int getBuildNumber()
    {
        return getVersion().getBuildNumberAsInt();
    }

    public void setBuildNumber(int i)
    {
        Config config = getConfig();
        config.setProperty(Version.BUILD_NUMBER, Integer.toString(i));
        dataVersion = null;
    }

    /**
     * Retrieve the version recorded in the data directory.
     *
     * @return Version
     */
    public Version getVersion()
    {
        if (dataVersion == null)
        {
            Properties prop = new Properties();
            prop.setProperty(Version.BUILD_DATE, getConfig().getProperty(Version.BUILD_DATE));
            prop.setProperty(Version.BUILD_NUMBER, getConfig().getProperty(Version.BUILD_NUMBER));
            prop.setProperty(Version.RELEASE_DATE, getConfig().getProperty(Version.RELEASE_DATE));
            prop.setProperty(Version.VERSION_NUMBER, getConfig().getProperty(Version.VERSION_NUMBER));
            dataVersion = Version.read(prop);
        }
        return dataVersion;
    }

    public void updateLicenseKey(String key)
    {
        getConfig().setProperty(LICENSE_KEY, key);
        licenseKey = null;
        license = null;
    }

    public String getLicenseKey()
    {
        if (licenseKey == null)
        {
            licenseKey = getConfig().getProperty(LICENSE_KEY);
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
        }
        return license;
    }

    /**
     * Retrieve the data directory.
     *
     * @return the configured data root directory.
     */
    public File getData()
    {
        return pulseData;
    }

    /**
     * @see com.zutubi.pulse.bootstrap.MasterUserPaths#getUserConfigRoot()
     */
    public File getUserConfigRoot()
    {
        if (userConfigRoot == null)
        {
            userConfigRoot = new File(pulseData, "config");
        }
        return userConfigRoot;
    }

    /**
     * @see com.zutubi.pulse.bootstrap.MasterUserPaths#getDatabaseRoot()
     */
    public File getDatabaseRoot()
    {
        if (databaseRoot == null)
        {
            databaseRoot = new File(pulseData, "database");
        }
        return databaseRoot;
    }

    /**
     * @see com.zutubi.pulse.bootstrap.MasterUserPaths#getProjectRoot()
     */
    public File getProjectRoot()
    {
        if (projectRoot == null)
        {
            projectRoot = new File(pulseData, "projects");
        }
        return projectRoot;
    }

    public File getUserRoot()
    {
        if(userRoot == null)
        {
            userRoot = new File(pulseData, "users");
        }
        return userRoot;
    }

    private Config getConfig()
    {
        if (config == null)
        {
            config = new FileConfig(getConfigFile());
        }
        return config;
    }

    private File getConfigFile()
    {
        return new File(pulseData, CONFIG_FILE_NAME);
    }
}
