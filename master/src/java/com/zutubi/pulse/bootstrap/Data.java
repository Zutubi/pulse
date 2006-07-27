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
    private File databaseRoot;

    public static final String CONFIG_FILE_NAME = "pulse.config.properties";

    private Properties config = null;
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
     * @throws IOException
     */
    public void init() throws IOException
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
     * @throws IOException
     */
    public void updateVersion(Version version) throws IOException
    {
        version.write(getConfig());
        IOUtils.write(getConfig(), getConfigFile());
    }

    public int getBuildNumber()
    {
        return getVersion().getBuildNumberAsInt();
    }

    public void setBuildNumber(int i)
    {
        try
        {
            Properties config = getConfig();
            config.put("build.number", Integer.toString(i));
            IOUtils.write(getConfig(), getConfigFile());
            dataVersion = null;
        }
        catch (IOException e)
        {
            LOG.severe("Failed to record build number to the config. Cause:" + e.getMessage(), e);
        }
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
            try
            {
                dataVersion = Version.read(getConfig());
            }
            catch (IOException e)
            {
                LOG.severe("Failed to load the config. Cause: " + e.getMessage(), e);
            }
        }
        return dataVersion;
    }

    public void updateLicenseKey(String key) throws IOException
    {
        getConfig().setProperty(LICENSE_KEY, key);
        IOUtils.write(getConfig(), getConfigFile());
    }

    public String getLicenseKey() throws IOException
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
            catch (IOException e)
            {
                LOG.severe("Failed to retrieve the license key.", e);
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
        return new File(pulseData, CONFIG_FILE_NAME);
    }
}
