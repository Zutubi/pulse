package com.zutubi.pulse.bootstrap;

import com.opensymphony.xwork.spring.SpringObjectFactory;
import com.zutubi.prototype.config.ConfigurationExtensionManager;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.ConfigurationRefactoringManager;
import com.zutubi.prototype.config.ConfigurationReferenceManager;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.config.ConfigurationStateManager;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.config.DefaultConfigurationProvider;
import com.zutubi.prototype.type.record.DelegatingHandleAllocator;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.conf.EnvConfig;
import com.zutubi.pulse.bootstrap.tasks.ProcessSetupStartupTask;
import com.zutubi.pulse.config.PropertiesWriter;
import com.zutubi.pulse.database.DatabaseConsole;
import com.zutubi.pulse.database.DriverRegistry;
import com.zutubi.pulse.events.DataDirectoryLocatedEvent;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.logging.LogConfigurationManager;
import com.zutubi.pulse.migrate.MigrationManager;
import com.zutubi.pulse.monitor.Monitor;
import com.zutubi.pulse.plugins.PluginManager;
import com.zutubi.pulse.prototype.config.admin.GlobalConfiguration;
import com.zutubi.pulse.restore.ArchiveException;
import com.zutubi.pulse.restore.ArchiveManager;
import com.zutubi.pulse.upgrade.UpgradeManager;
import com.zutubi.util.IOUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.logging.Logger;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FilenameFilter;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Works through the setup process, gradually starting Pulse as more bits and
 * pieces become available.
 */
public class DefaultSetupManager implements SetupManager
{
    private static final Logger LOG = Logger.getLogger(DefaultSetupManager.class);

    /**
     * Set to true during startup if the setup manager is run.
     */
    public static boolean initialInstallation = false;

    private MasterConfigurationManager configurationManager;
    private UpgradeManager upgradeManager;
    private MigrationManager migrationManager;
    private EventManager eventManager;

    /**
     * Contexts for Stage A: the config subsystem.
     */
    private List<String> configContexts = new LinkedList<String>();

    private List<String> migrationContext = new LinkedList<String>();

    /**
     * Contexts for Stage B: database
     */
    private List<String> dataContexts = new LinkedList<String>();

    /**
     * Contexts for Stage C: restore
     */
    private List<String> restoreContexts = new LinkedList<String>();

    private List<String> licenseContexts = new LinkedList<String>();

    /**
     * Contexts for Stage D: the upgrade system.
     */
    private List<String> upgradeContexts = new LinkedList<String>();

    /**
     * Contexts for Stage E: setup / configuration.
     */
    private List<String> setupContexts = new LinkedList<String>();

    /**
     * Contexts for Stage F: application startup.
     */
    private List<String> startupContexts = new LinkedList<String>();

    /**
     * Contexts for Stage G: post-startup.
     */
    private List<String> postStartupContexts = new LinkedList<String>();

    private SetupState state = SetupState.STARTING;

    private boolean promptShown = false;
    private DatabaseConsole databaseConsole;

    private ProcessSetupStartupTask setupCallback;
    // Note that this is null until part way through startup
    private ConfigurationProvider configurationProvider;

    public SetupState getCurrentState()
    {
        return state;
    }

    public void startSetupWorkflow(ProcessSetupStartupTask processSetupStartupTask)
    {
        this.setupCallback = processSetupStartupTask;

        state = SetupState.STARTING;

        // record the startup configuration so that it can be reused next time.
        createExternalConfigFileIfRequired();

        initialiseConfigurationSystem();

        if (isDataRequired())
        {
            printConsoleMessage("No data path configured, requesting via web UI...");

            // request data input.
            state = SetupState.DATA;
            showPrompt();
            return;
        }
        requestDataComplete();
    }

    //TODO: move this into the configuration managers.  It is specific to them, and should not be here.
    // Part of the configuration system initialisation.  This is picked up by the configuration manager next time
    // round.
    private void createExternalConfigFileIfRequired()
    {
        try
        {
            // If the user configuration file does not exist, create it now.
            EnvConfig envConfig = configurationManager.getEnvConfig();
            SystemConfiguration sysConfig = configurationManager.getSystemConfig();

            String externalConfig = envConfig.getPulseConfig();
            if (!TextUtils.stringSet(externalConfig))
            {
                // default is something like ~/.pulse2/config.properties
                externalConfig = envConfig.getDefaultPulseConfig(MasterConfigurationManager.CONFIG_DIR);
            }
            File configFile = new File(externalConfig);
            if (!configFile.isAbsolute())
            {
                configFile = configFile.getCanonicalFile();
            }

            printConsoleMessage("Using config file '%s'.", configFile.getAbsolutePath());
            if (!configFile.isFile())
            {
                printConsoleMessage("Config file does not exist, creating a default one.");

                // copy the template file into the config location.
                SystemPaths paths = configurationManager.getSystemPaths();
                File configTemplate = new File(paths.getConfigRoot(), "config.properties.template");
                IOUtils.copyTemplate(configTemplate, configFile);

                // write the default configuration to this template file.
                // There needs to be a way to do this without duplicating the default configuration data.
                // Unfortunately, the defaults are currently hidden by any system properties that over ride them...
                Properties props = new Properties();
                props.setProperty(SystemConfiguration.CONTEXT_PATH, "/");
                props.setProperty(SystemConfiguration.WEBAPP_PORT, "8080");
                props.setProperty(SystemConfiguration.PULSE_DATA, (sysConfig.getDataPath() != null ? sysConfig.getDataPath() : ""));

                PropertiesWriter writer = new PropertiesWriter();
                writer.write(configFile, props);
            }
        }
        catch (IOException e)
        {
            System.err.println("WARNING: " + e.getMessage());
        }
    }

    private void initialiseConfigurationSystem()
    {
        loadContexts(configContexts);

        ConfigurationRegistry configurationRegistry = ComponentContext.getBean("configurationRegistry");
        configurationRegistry.initSetup();
    }

    public void requestDataComplete()
    {
        // If this is the first time this directory is being used as a data directory, then we need
        // to ensure that it is initialised. If we are working with an already existing directory,
        // then it will have been initialised and no re-initialisation is required (or allowed).
        Data d = configurationManager.getData();
        printConsoleMessage("Using data path '%s'.", d.getData().getAbsolutePath());
        if (!d.isInitialised())
        {
            printConsoleMessage("Empty data directory, initialising...");
            configurationManager.getData().init(configurationManager.getSystemPaths());
            printConsoleMessage("Data directory initialised.");
        }

        eventManager.publish(new DataDirectoryLocatedEvent(this));

        loadSystemProperties();
        handleDbSetup();
    }

    //TODO: replace this with a configuration listener that monitors for the DataDirectoryLocatedEvent, and
    //TODO: loads the system.properties file accordingly.  Why? To keep all of the config work in one place.
    //TODO: At the moment, it is split up into little bits in lots of places which makes it awkward.
    // I don't get this: won't moving this code into a listener split things up more?  Currently this class
    // is the closest thing we have to 'one place' to look for startup stuff.
    private void loadSystemProperties()
    {
        File propFile = new File(configurationManager.getUserPaths().getUserConfigRoot(), "system.properties");
        if (propFile.exists())
        {
            FileInputStream is = null;
            try
            {
                is = new FileInputStream(propFile);
                System.getProperties().load(is);
            }
            catch (IOException e)
            {
                LOG.warning("Unable to load system properties: " + e.getMessage(), e);
            }
            finally
            {
                IOUtils.close(is);
            }
        }
    }

    private void handleDbSetup()
    {
        ensureDriversRegisteredAndLoaded();
        
        File databaseConfig = new File(configurationManager.getData().getUserConfigRoot(), "database.properties");
        if (!databaseConfig.exists())
        {
            printConsoleMessage("No database setup, requesting details via web UI...");
            state = SetupState.DATABASE;
            showPrompt();
            return;
        }

        handleDbMigration();
    }

    public void handleDbMigration()
    {
        // if db migration is requested, trigger the flow.
        loadContexts(migrationContext);

        if (isDatabaseMigrationRequested())
        {
            printConsoleMessage("Database migration requested, requesting details via web UI.");
            state = SetupState.MIGRATE;

            showPrompt();
            return;
        }

        requestDbComplete();
    }

    private boolean isDatabaseMigrationRequested()
    {
        return migrationManager.isRequested();
    }

    public void doCancelMigrationRequest()
    {
        requestDbComplete();
    }

    public void requestDbComplete()
    {
        state = SetupState.STARTING;

        loadContexts(dataContexts);

        // create the database based on the hibernate configuration.
        databaseConsole = (DatabaseConsole) ComponentContext.getBean("databaseConsole");
        if (databaseConsole.isEmbedded())
        {
            printConsoleMessage("Using embedded database (only recommended for evaluation purposes).");
        }
        else
        {
            printConsoleMessage("Using external database '%s'.", databaseConsole.getConfig().getUrl());
        }

        if (!databaseConsole.schemaExists())
        {
            printConsoleMessage("Database schema does not exist, initialising...");
            try
            {
                databaseConsole.createSchema();
            }
            catch (SQLException e)
            {
                throw new StartupException("Failed to create the database schema. Cause: " + e.getMessage());
            }
            printConsoleMessage("Database initialised.");
        }

        handleRestorationProcess();
    }

    private void ensureDriversRegisteredAndLoaded()
    {
        try
        {
            DriverRegistry driverRegistry = configurationManager.getDriverRegistry();

            // ensure that the embedded driver is registered.
            if (!driverRegistry.isRegistered("org.hsqldb.jdbcDriver"))
            {
                driverRegistry.register("org.hsqldb.jdbcDriver");
            }

            // ensure backward compatibility with existing driver directories -
            // awkward startup timing here - if we call the getDatabaseConfig too early, the config object is initialised
            // badly.  However, we need ot know if we can load that data, so check for the file.
            //TODO: This needs to be resolved properly.
            File databaseConfig = new File(configurationManager.getData().getUserConfigRoot(), "database.properties");
            if (databaseConfig.isFile())
            {
                String driverClassName = configurationManager.getDatabaseConfig().getDriverClassName();
                if (TextUtils.stringSet(driverClassName) && !driverRegistry.isRegistered(driverClassName))
                {
                    File driverRoot = configurationManager.getData().getDriverRoot();
                    File[] driverJars = driverRoot.listFiles(new FilenameFilter()
                    {
                        public boolean accept(File dir, String name)
                        {
                            return !name.equals(".registry");
                        }
                    });

                    for (File driverJar : driverJars)
                    {
                        try
                        {
                            driverRegistry.register(driverClassName, driverJar);
                            break;
                        }
                        catch (IOException e)
                        {
                            // noop.
                        }
                    }
                    if (!driverRegistry.isRegistered(driverClassName))
                    {
                        LOG.warning("Failed to locate '" + driverClassName + "' from any of the jars in " + driverRoot.getCanonicalPath() +".");
                    }
                }
            }
        }
        catch (Exception e)
        {
            LOG.severe(e);
            throw new StartupException("Unable to load database driver: " + e.getMessage(), e);
        }
    }

    public void requestRestoreComplete(boolean restored)
    {
        if (databaseConsole.isEmbedded())
        {
            printConsoleMessage("Compacting embedded database.  This may take some time.");
        }

        databaseConsole.postRestoreHook(restored);
        
        linkUserTemplates();

        initialiseConfigurationPersistence();

        loadContexts(licenseContexts);

        printConsoleMessage("Checking license...");
        if (isLicenseRequired())
        {
            printConsoleMessage("No valid license found, requesting via web UI...");
            //TODO: we need to provide some feedback to the user about what / why there current license
            //TODO: (if one exists) is not sufficient.
            state = SetupState.LICENSE;
            showPrompt();
            return;
        }
        requestLicenseComplete();
    }

    private void linkUserTemplates()
    {
        File userTemplateRoot = configurationManager.getUserPaths().getUserTemplateRoot();
        if (userTemplateRoot.isDirectory())
        {
            Configuration freemarkerConfiguration = ComponentContext.getBean("freemarkerConfiguration");
            TemplateLoader existingLoader = freemarkerConfiguration.getTemplateLoader();
            try
            {
                TemplateLoader userLoader = new FileTemplateLoader(userTemplateRoot);
                freemarkerConfiguration.setTemplateLoader(new MultiTemplateLoader(new TemplateLoader[]{userLoader, existingLoader}));
            }
            catch (IOException e)
            {
                LOG.warning(e);
            }
        }
    }

    public void requestLicenseComplete()
    {
        printConsoleMessage("License accepted.");

        // License is allowed to run this version of pulse. Therefore, it is okay to go ahead with an upgrade.
        databaseConsole.postSchemaHook();
        loadContexts(upgradeContexts);

        if (isUpgradeRequired())
        {
            printConsoleMessage("Upgrade is required: existing data version '" + configurationManager.getData().getVersion().getVersionNumber() + ", Pulse version " + Version.getVersion().getVersionNumber() + "...");
            state = SetupState.UPGRADE;
            showPrompt();
            return;
        }

        updateVersionIfNecessary();

        requestUpgradeComplete(false);
    }

    private void initialiseConfigurationPersistence()
    {
        RecordManager recordManager = ComponentContext.getBean("recordManager");
        PluginManager pluginManager = ComponentContext.getBean("pluginManager");
        DelegatingHandleAllocator handleAllocator = ComponentContext.getBean("handleAllocator");
        ConfigurationPersistenceManager configurationPersistenceManager = ComponentContext.getBean("configurationPersistenceManager");
        ConfigurationReferenceManager configurationReferenceManager = ComponentContext.getBean("configurationReferenceManager");
        ConfigurationTemplateManager configurationTemplateManager = ComponentContext.getBean("configurationTemplateManager");
        ConfigurationRefactoringManager configurationRefactoringManager = ComponentContext.getBean("configurationRefactoringManager");
        ConfigurationRegistry configurationRegistry = ComponentContext.getBean("configurationRegistry");
        ConfigurationExtensionManager configurationExtensionManager = ComponentContext.getBean("configurationExtensionManager");
        ConfigurationStateManager configurationStateManager = ComponentContext.getBean("configurationStateManager");

        recordManager.init();

        handleAllocator.setDelegate(recordManager);
        configurationPersistenceManager.setRecordManager(recordManager);
        configurationReferenceManager.setRecordManager(recordManager);
        configurationTemplateManager.setRecordManager(recordManager);
        configurationRefactoringManager.setRecordManager(recordManager);
        configurationRegistry.init();

        configurationExtensionManager.setPluginManager(pluginManager);
        configurationExtensionManager.init();

        configurationStateManager.setRecordManager(recordManager);

        configurationTemplateManager.init();

        LogConfigurationManager logConfigurationManager = ComponentContext.getBean("logConfigurationManager");
        logConfigurationManager.init();
    }

    public void requestUpgradeComplete(boolean changes)
    {
        if (changes)
        {
            printConsoleMessage("Upgrade complete.");
        }
        databaseConsole.postUpgradeHook(changes);

        // Remove the upgrade context from the ComponentContext stack / namespace.
        // They are no longer required.
        ComponentContext.pop();
        loadContexts(setupContexts);

        if (isSetupRequired())
        {
            printConsoleMessage("Database empty, requesting setup via web UI...");
            state = SetupState.SETUP;
            initialInstallation = true;
            showPrompt();
            return;
        }
        requestSetupComplete(false);
    }

    public void requestSetupComplete(boolean setupWizard)
    {
        if (setupWizard)
        {
            printConsoleMessage("Setup wizard complete.");
        }

        state = SetupState.STARTING;

        // load the remaining contexts.
        // The user contexts are separate as otherwise there is a cycle in
        // dependencies that confuses spring and then me (as spring gives a
        // useless error message).
        loadContexts(startupContexts);
        loadContexts(postStartupContexts);

        DefaultConfigurationProvider configurationProvider = ComponentContext.getBean("configurationProvider");
        configurationProvider.init();
        this.configurationProvider = configurationProvider;

        setupCallback.finaliseSetup();
    }

    private void loadContexts(List<String> contexts)
    {
        ComponentContext.addClassPathContextDefinitions(contexts.toArray(new String[contexts.size()]));
        ComponentContext.autowire(this);

        // xwork object factory refresh - need to ensure that it has a reference to the latest spring context.
        SpringObjectFactory objFact = (SpringObjectFactory) ComponentContext.getBean("xworkObjectFactory");
        if (objFact != null)
        {
            objFact.setApplicationContext(ComponentContext.getContext());
        }
    }

    private boolean isDataRequired()
    {
        return configurationManager.getData() == null;
    }

    private boolean isLicenseRequired()
    {
        // if we are not licensed, then request that a license be provided.
        return !LicenseHolder.hasAuthorization(LicenseHolder.AUTH_RUN_PULSE);
    }

    private boolean isUpgradeRequired()
    {
        return upgradeManager.isUpgradeRequired();
    }

    private boolean isSetupRequired()
    {
        ConfigurationTemplateManager configurationTemplateManager = ComponentContext.getBean("configurationTemplateManager");
        return configurationTemplateManager.getRecord(ConfigurationRegistry.USERS_SCOPE).size() == 0;
    }

    private void updateVersionIfNecessary()
    {
        // is the version reported in the data directory the same as the version reported by this
        // pulse version?
        Data d = configurationManager.getData();

        Version dataVersion = d.getVersion();
        if (dataVersion.getBuildNumberAsInt() < Version.getVersion().getBuildNumberAsInt())
        {
            d.updateVersion(Version.getVersion());
        }
    }

    public static void printConsoleMessage(String format, Object... args)
    {
        String date = "[" + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(new Date()) + "] ";
        System.err.printf(date + format + "\n", args);
    }

    /**
     * Prompt the user via the command line to go to the Web UI to continue the setup process.
     */
    private void showPrompt()
    {
        // We ensure that we only ever prompt the user once.
        if (!promptShown)
        {
            String baseUrl = null;

            // depending on whether or not we are prompting from an existing installation or a new installation will
            // determine whether or not the baseUrl has been configured via the configuration system (and whether or
            // not the configurationProvider is available).  ie: the ConfigurationProvider is available during the
            // upgrade process - which only happens when we are working with an existing installation.
            if (configurationProvider != null)
            {
                GlobalConfiguration config = configurationProvider.get(GlobalConfiguration.class);
                if (config != null)
                {
                    baseUrl = config.getBaseUrl();
                }
            }

            if (!TextUtils.stringSet(baseUrl))
            {
                // fall back to the default host url.
                SystemConfigurationSupport systemConfig = (SystemConfigurationSupport) configurationManager.getSystemConfig();
                baseUrl = systemConfig.getHostUrl();
            }

            // WARNING: sometimes calculating the host url and sometimes using a configured setting (from a previous
            //          installation or possibly a restore) results in possibly inconsistent behaviour.  It may be
            //          worth while always using the calculated host url since this is being printed on the local host.

            printConsoleMessage("Now go to %s and follow the prompts.", baseUrl);
            promptShown = true;
        }
    }

    //---( workflow for the restoration process.  Should probably shift this out of the manager...,
    // all this manager is interested in are co-ordinating the various steps, not the workflow of each step.  )---

    private ArchiveManager archiveManager;

    private boolean isRestoreRequested()
    {
        // check for the existance of a PULSE_DATA/restore/archive.zip
        File archive = getArchiveFile();
        if (archive == null)
        {
            return false;
        }
        if (archive.exists())
        {
            return true;
        }
        printConsoleMessage("Specified restore archive file " + archive.getAbsolutePath() + " does not exist. Skipping restore.");
        return false;
    }

    /**
     * The archive file is any zip file located in the PULSE_DATA/restore directory.  It is required that only
     * one zip file be present.
     *
     * @return the zip file containing the archive.
     */
    //TODO: 1) pick the latest archive if multiple are detected?
    private File getArchiveFile()
    {
        // there are two ways to trigger a restore.
        // a) --restore filename on the command line.
        // b) dropping a zip into the PULSE_DATA/restore directory
        SystemConfiguration systemConfig = configurationManager.getSystemConfig();
        if (systemConfig.getRestoreFile() != null)
        {
            return new File(systemConfig.getRestoreFile());
        }

        MasterUserPaths paths = configurationManager.getUserPaths();
        if (paths != null)
        {
            File restoreDir = paths.getRestoreRoot();
            File[] ls = restoreDir.listFiles(new FileFilter()
            {
                public boolean accept(File file)
                {
                    return file.isFile() && file.getName().endsWith(".zip");
                }
            });
            if (ls == null || ls.length == 0)
            {
                return null;
            }
            if (ls.length > 1)
            {
                // too many files in the restore directory, do not know which one to pick.
                return null;
            }
            return ls[0];
        }

        return null;
    }

    public void handleRestorationProcess()
    {
        //TODO: only need to load restoreContexts if restore is requested? maybe load these statically...
        loadContexts(restoreContexts);

        if (isRestoreRequested())
        {
            File backup = getArchiveFile();
            try
            {
                printConsoleMessage("Restoring from archive file: " + backup.getCanonicalPath());
            }
            catch (IOException e)
            {
                printConsoleMessage("Restoring from archive file: " + backup.getAbsolutePath());
            }


            try
            {
                archiveManager.prepareRestore(backup);
            }
            catch (ArchiveException e)
            {
                LOG.severe("Restore preparation failed: " + e.getMessage(), e);
            }

            // show restoration preview page.
            state = SetupState.RESTORE;
            showPrompt();
            return;
        }

        requestRestoreComplete(false);
    }

    // continue selected on the restoration preview page.
    public void doExecuteRestorationRequest()
    {
        Monitor monitor = archiveManager.getTaskMonitor();
        if (!monitor.isStarted())
        {
            archiveManager.restoreArchive();
        }
    }

    public void doCancelRestorationRequest() throws IOException
    {
        requestRestoreComplete(false);
    }

    public void doCompleteRestoration() throws IOException
    {
        requestRestoreComplete(true);
    }

    //---( end )

    public void setUpgradeManager(UpgradeManager upgradeManager)
    {
        this.upgradeManager = upgradeManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setConfigContexts(List<String> configContexts)
    {
        this.configContexts = configContexts;
    }

    public void setDataContexts(List<String> dataContexts)
    {
        this.dataContexts = dataContexts;
    }

    public void setLicenseContexts(List<String> licenseContexts)
    {
        this.licenseContexts = licenseContexts;
    }

    public void setRestoreContexts(List<String> restoreContexts)
    {
        this.restoreContexts = restoreContexts;
    }

    public void setSetupContexts(List<String> setupContexts)
    {
        this.setupContexts = setupContexts;
    }

    public void setStartupContexts(List<String> startupContexts)
    {
        this.startupContexts = startupContexts;
    }

    public void setPostStartupContexts(List<String> postStartupContexts)
    {
        this.postStartupContexts = postStartupContexts;
    }

    public void setUpgradeContexts(List<String> upgradeContexts)
    {
        this.upgradeContexts = upgradeContexts;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setArchiveManager(ArchiveManager archiveManager)
    {
        this.archiveManager = archiveManager;
    }

    public void setMigrationContext(List<String> migrationContext)
    {
        this.migrationContext = migrationContext;
    }

    public void setMigrationManager(MigrationManager migrationManager)
    {
        this.migrationManager = migrationManager;
    }
}
