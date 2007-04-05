package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.config.Config;
import com.zutubi.pulse.config.FileConfig;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.ZipUtils;
import com.zutubi.pulse.util.logging.Logger;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * The data object provides an interface to the configured data directory,
 * its layout and its data.
 * <p>
 * The data directory is layed out as follows:
 * <pre>
 * data/
 *     config/      user configuration files
 *     database/    the embedded HSQL database
 *     projects/    build artifacts
 *     backups/     backups directory.     
 *
 * pulse.config.properties: core configuration properties, contain version and license details amongst other things
 * </pre>
 *
 * </p>
 */
public class Data implements MasterUserPaths
{
    private static final Logger LOG = Logger.getLogger(Data.class);

    public static final String CONFIG_FILE_NAME = "pulse.config.properties";
    private static final String LICENSE_KEY = "license.key";

    private final File pulseData;

    private Version dataVersion;
    private File userConfigRoot;
    private File projectRoot;
    private File userRoot;
    private File databaseRoot;
    private File userTemplateRoot;
    private File recordRoot;

    private File backupRoot;

    private Config config = null;

    public Data(File dataDir)
    {
        this.pulseData = dataDir;
    }

    /**
     * The data directory MUST be initialised before the application can start.
     *
     * @return true if the data is initialised
     * @see #init(SystemPaths)
     */
    public boolean isInitialised()
    {
        return pulseData.exists() && getConfigFile().exists();
    }

    /**
     * Initialise the data directory. This will ensure that the necessary directories
     * and configuration files are setup.
     *
     * @param systemPaths system paths
     */
    public void init(SystemPaths systemPaths)
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

        transferExampleTemplates(systemPaths);
        transferDatabaseConfig(systemPaths);
    }

    /**
     * Create a fresh backup of this data directory.
     *
     * @param systemPaths instance
     *
     * @throws IOException if a problem occurs generating the backup.
     */
    public void backup(SystemPaths systemPaths) throws IOException
    {
        String filename = String.format("auto-backup-%s.zip", getVersion().getVersionNumber());

        // no need to create a backup is it already exists.
        File backup = new File(getBackupRoot(), filename);
        if (backup.isFile() && !backup.delete())
        {
            throw new IOException(String.format("Failed to remove previous backup '%s'. This " +
                    "file prevents a new backup from being generated.", backup.getAbsolutePath()));
        }

        // copy the files into the system tmpRoot.
        File tmpBackup = new File(systemPaths.getTmpRoot(), filename);
        tmpBackup.mkdirs();

        // Are we running an embedded database? If so, we need to back it up.
        DatabaseConsole databaseConsole = (DatabaseConsole) ComponentContext.getBean("databaseConsole");
        if (databaseConsole.isEmbedded())
        {
            // trigger a checkpoint call on the database to compact the data.
            HSQLDBUtils.compactDatabase((DataSource) ComponentContext.getBean("dataSource"));
            File dest = new File(tmpBackup, "database");
            dest.mkdir();
            conditionalCopy(dest, new File(getDatabaseRoot(), "db.backup"));
            conditionalCopy(dest, new File(getDatabaseRoot(), "db.log"));
            conditionalCopy(dest, new File(getDatabaseRoot(), "db.properties"));
            conditionalCopy(dest, new File(getDatabaseRoot(), "db.data"));
            conditionalCopy(dest, new File(getDatabaseRoot(), "db.script"));
        }

        FileSystemUtils.copy(new File(tmpBackup, CONFIG_FILE_NAME), getConfigFile());

        ZipUtils.createZip(backup, tmpBackup, null);

        FileSystemUtils.rmdir(tmpBackup);
        // - done.
    }

    private void conditionalCopy(File dest, File file) throws IOException
    {
        if(file.exists())
        {
            FileSystemUtils.copy(dest, file);
        }
    }

    public void restore()
    {
        // extract the backed up files.
        // need to shutdown the database, restore the backed up files, and restart.

    }

    /**
     * Update the version recorded in the data directory.
     *
     * @param version is the new version
     */
    public void updateVersion(Version version)
    {
        getConfig().setProperty(Version.BUILD_DATE, version.getBuildDate());
        getConfig().setProperty(Version.BUILD_NUMBER, version.getBuildNumber());
        getConfig().setProperty(Version.RELEASE_DATE, version.getReleaseDate());
        getConfig().setProperty(Version.VERSION_NUMBER, version.getVersionNumber());
    }

    public void transferExampleTemplates(SystemPaths systemPaths)
    {
        // Copy across some example template files to the user's template root
        File userTemplateRoot = getUserTemplateRoot();
        if (!userTemplateRoot.isDirectory())
        {
            if(!userTemplateRoot.getParentFile().isDirectory())
            {
                userTemplateRoot.getParentFile().mkdirs();
            }

            List<File> templateRoots = systemPaths.getTemplateRoots();
            for(File templateRoot: templateRoots)
            {
                File examplesDir = new File(templateRoot, "examples");
                if(examplesDir.isDirectory())
                {
                    try
                    {
                        FileSystemUtils.copy(userTemplateRoot, examplesDir);
                        break;
                    }
                    catch (IOException e)
                    {
                        LOG.warning("Unable to copy example templates to '" + userTemplateRoot.getAbsolutePath() + "'", e);
                    }
                }
            }
        }
    }

    /**
     * Transfer the database.properties.template file into the data directory so that it is available for pulse
     * to create the database connection.
     *
     * @param systemPaths
     *
     * @throws IOException if there is a problem transferring the template.
     */
    private void transferDatabaseConfig(SystemPaths systemPaths) throws StartupException
    {
        try
        {
            File databaseConfig = new File(getUserConfigRoot(), "database.properties");
            
            // if the database.properties file already exists in the database directory, do not over write it.
            // This allows pre-configured database.properties files to be used when pulse starts up.
            if (!databaseConfig.exists())
            {
                File databaseConfigTemplate = new File(systemPaths.getConfigRoot(), "database.properties.template");
                IOUtils.copyTemplate(databaseConfigTemplate, databaseConfig);
            }
        }
        catch (IOException e)
        {
            throw new StartupException("Failed to create the database configuration file.", e);
        }
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

    public String getLicenseKey()
    {
        return getConfig().getProperty(LICENSE_KEY);
    }

    public void setLicenseKey(String licenseKey)
    {
        getConfig().setProperty(LICENSE_KEY, licenseKey);
    }

    //---( implementation of the data resolver interface. )---

    /**
     * Retrieve the data directory.
     *
     * @return the configured data root directory.
     */
    public File getData()
    {
        return pulseData;
    }

    //---( implementation of the user paths interface )---

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

    public File getUserTemplateRoot()
    {
        if(userTemplateRoot == null)
        {
            userTemplateRoot = new File(getUserConfigRoot(), "templates");
        }
        return userTemplateRoot;
    }

    public File getRecordRoot()
    {
        if(recordRoot == null)
        {
            recordRoot = new File(getData(), "records");
        }
        return recordRoot;
    }

    private Config getConfig()
    {
        if (config == null)
        {
            config = new FileConfig(getConfigFile());
        }
        return config;
    }

    public File getBackupRoot()
    {
        if (backupRoot == null)
        {
            backupRoot = new File(pulseData, "backups");
        }
        return backupRoot;
    }

    private File getConfigFile()
    {
        return new File(pulseData, CONFIG_FILE_NAME);
    }
}
