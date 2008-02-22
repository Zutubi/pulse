package com.zutubi.pulse.bootstrap;

import com.opensymphony.xwork.spring.SpringObjectFactory;
import com.zutubi.prototype.config.ConfigurationExtensionManager;
import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.prototype.config.ConfigurationReferenceManager;
import com.zutubi.prototype.config.ConfigurationRegistry;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.config.DefaultConfigurationProvider;
import com.zutubi.prototype.type.record.DelegatingHandleAllocator;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.conf.EnvConfig;
import com.zutubi.pulse.bootstrap.tasks.ProcessSetupStartupTask;
import com.zutubi.pulse.config.PropertiesWriter;
import com.zutubi.pulse.database.DatabaseConsole;
import com.zutubi.pulse.events.DataDirectoryLocatedEvent;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.logging.LogConfigurationManager;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.plugins.PluginManager;
import com.zutubi.pulse.prototype.config.admin.GeneralAdminConfiguration;
import com.zutubi.pulse.restore.ArchiveManager;
import com.zutubi.pulse.restore.ProgressMonitor;
import com.zutubi.pulse.restore.ArchiveException;
import com.zutubi.pulse.upgrade.UpgradeManager;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.IOUtils;
import com.zutubi.util.TextUtils;
import com.zutubi.util.logging.Logger;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileFilter;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class DefaultSetupManager implements SetupManager
{
    private static final Logger LOG = Logger.getLogger(DefaultSetupManager.class);

    /**
     * Set to true during startup if the setup manager is run.
     */
    public static boolean initialInstallation = false;

    private MasterConfigurationManager configurationManager;
    private UserManager userManager;
    private UpgradeManager upgradeManager;
    private EventManager eventManager;

    /**
     * Contexts for Stage A: the config subsystem.
     */
    private List<String> configContexts = new LinkedList<String>();

    private List<String> restoreContexts = new LinkedList<String>();

    /**
     * Contexts for Stage B: the database.
     */
    private List<String> daoContexts = new LinkedList<String>();

    // Stage B.2
    private List<String> licenseContexts = new LinkedList<String>();

    /**
     * Contexts for Stage C: the upgrade system.
     */
    private List<String> upgradeContexts = new LinkedList<String>();

    /**
     * Contexts for Stage D: setup / configuration.
     */
    private List<String> setupContexts = new LinkedList<String>();

    /**
     * Contexts for Stage E: application startup.
     */
    private List<String> startupContexts = new LinkedList<String>();

    /**
     * Contexts for Stage F: post-startup.
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
            if (!configFile.isFile())
            {
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

        LogConfigurationManager logConfigurationManager = ComponentContext.getBean("logConfigurationManager");
        logConfigurationManager.applyConfig();
    }

    public void requestDataComplete()
    {
        // If this is the first time this directory is being used as a data directory, then we need
        // to ensure that it is initialised. If we are working with an already existing directory,
        // then it will have been initialised and no re-initialisation is required (or allowed).
        Data d = configurationManager.getData();
        if (!d.isInitialised())
        {
            configurationManager.getData().init(configurationManager.getSystemPaths());
        }

        eventManager.publish(new DataDirectoryLocatedEvent(this));

        loadSystemProperties();

        handleRestorationProcess();
    }

    public void requestRestoreComplete()
    {
        linkUserTemplates();

        // load db contexts...
        loadContexts(daoContexts);
        initialiseConfigurationPersistence();

        loadContexts(licenseContexts);

        state = SetupState.STARTING;
        if (isLicenseRequired())
        {
            //TODO: we need to provide some feedback to the user about what / why their current license
            //TODO: (if one exists) is not sufficient.
            state = SetupState.LICENSE;
            showPrompt();
            return;
        }
        requestLicenseComplete();
    }

    //TODO: replace this with a configuration listener that monitors for the DataDirectoryLocatedEvent, and
    //TODO: loads the system.properties file accordingly.  Why? To keep all of the config work in one place.
    //TODO: At the moment, it is split up into little bits in lots of places which makes it awkward.
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
        state = SetupState.STARTING;

        // License is allowed to run this version of pulse. Therefore, it is okay to go ahead with an upgrade.

        // create the database based on the hibernate configuration.
        databaseConsole = (DatabaseConsole) ComponentContext.getBean("databaseConsole");
        if (!databaseConsole.schemaExists())
        {
            try
            {
                databaseConsole.createSchema();
            }
            catch (SQLException e)
            {
                throw new StartupException("Failed to create the database schema. Cause: " + e.getMessage());
            }
        }

        databaseConsole.postSchemaHook();
        loadContexts(upgradeContexts);

        if (isUpgradeRequired())
        {
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
        ConfigurationRegistry configurationRegistry = ComponentContext.getBean("configurationRegistry");
        ConfigurationExtensionManager configurationExtensionManager = ComponentContext.getBean("configurationExtensionManager");
        DefaultConfigurationProvider configurationProvider = ComponentContext.getBean("configurationProvider");

        recordManager.init();

        handleAllocator.setDelegate(recordManager);
        configurationPersistenceManager.setRecordManager(recordManager);
        configurationReferenceManager.setRecordManager(recordManager);
        configurationTemplateManager.setRecordManager(recordManager);
        configurationRegistry.init();

        configurationExtensionManager.setPluginManager(pluginManager);
        configurationExtensionManager.init();

        configurationTemplateManager.init();
        configurationProvider.init();
        this.configurationProvider = configurationProvider;
    }

    public void requestUpgradeComplete(boolean changes)
    {
        databaseConsole.postUpgradeHook(changes);

        state = SetupState.STARTING;

        // Remove the upgrade context from the ComponentContext stack / namespace.
        // They are no longer required.
        ComponentContext.pop();

        loadContexts(setupContexts);

        if (isSetupRequired())
        {
            state = SetupState.SETUP;
            initialInstallation = true;
            return;
        }
        requestSetupComplete();
    }

    public void requestSetupComplete()
    {
        state = SetupState.STARTING;

        // load the remaining contexts.
        loadContexts(startupContexts);
        loadContexts(postStartupContexts);

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
        return userManager.getUserCount() == 0;
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
                GeneralAdminConfiguration config = configurationProvider.get(GeneralAdminConfiguration.class);
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

            System.err.println("Now go to " + baseUrl + " and follow the prompts.");
            promptShown = true;
        }
    }

    //---( workflow for the restoration process.  Should probably shift this out of the manager...,
    // all this manager is interested in are co-ordinating the various steps, not the workflow of each step.  )---

    private ArchiveManager archiveManager;

    private boolean isRestoreRequested()
    {
        // check for the existance of a PULSE_DATA/restore/archive.zip
        return getArchiveFile() != null;
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
        UserPaths paths = configurationManager.getUserPaths();
        if (paths != null)
        {
            File restoreDir = new File(paths.getData(), "restore");
            File[] ls = restoreDir.listFiles(new FileFilter()
            {
                public boolean accept(File file)
                {
                    return file.getName().endsWith(".zip");
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

            try
            {
                archiveManager.prepareRestore(getArchiveFile());
            }
            catch (ArchiveException e)
            {
                e.printStackTrace();
            }

            // show restoration preview page.
            state = SetupState.RESTORE;
            showPrompt();
            return;
        }

        requestRestoreComplete();
    }

    // continue selected on the restoration preview page.
    public void doExecuteRestorationRequest()
    {
        ProgressMonitor monitor = archiveManager.getMonitor();
        if (!monitor.isStarted())
        {
            archiveManager.restoreArchive();
        }
    }

    public void doCancelRestorationRequest() throws IOException
    {
        // delete the PULSE_DATA/restore/archive.zip
        FileSystemUtils.delete(getArchiveFile());

        requestRestoreComplete();
    }

    public void doCompleteRestoration() throws IOException
    {
        // remove the archive since the restoration is complete.
        FileSystemUtils.delete(getArchiveFile());

        requestRestoreComplete();
    }

    //---( end )

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

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

    public void setDaoContexts(List<String> daoContexts)
    {
        this.daoContexts = daoContexts;
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
}
