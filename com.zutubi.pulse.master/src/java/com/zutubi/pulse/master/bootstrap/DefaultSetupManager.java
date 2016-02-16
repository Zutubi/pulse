package com.zutubi.pulse.master.bootstrap;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.spring.SpringObjectFactory;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.events.DataDirectoryLocatedEvent;
import com.zutubi.pulse.core.plugins.PluginManager;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.core.util.config.EnvConfig;
import com.zutubi.pulse.master.bootstrap.tasks.ProcessSetupStartupTask;
import com.zutubi.pulse.master.database.DatabaseConsole;
import com.zutubi.pulse.master.database.DriverRegistry;
import com.zutubi.pulse.master.license.LicenseHolder;
import com.zutubi.pulse.master.license.LicenseManager;
import com.zutubi.pulse.master.migrate.MigrateDatabaseTypeConfiguration;
import com.zutubi.pulse.master.migrate.MigrationManager;
import com.zutubi.pulse.master.model.Role;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.restore.ArchiveException;
import com.zutubi.pulse.master.restore.RestoreManager;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.ConfigurationExtensionManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.admin.EmailConfiguration;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.group.BuiltinGroupConfiguration;
import com.zutubi.pulse.master.tove.config.group.ServerPermission;
import com.zutubi.pulse.master.tove.config.group.UserGroupConfiguration;
import com.zutubi.pulse.master.tove.config.setup.*;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;
import com.zutubi.pulse.master.tove.config.user.UserConfigurationCreator;
import com.zutubi.pulse.master.tove.config.user.contacts.EmailContactConfiguration;
import com.zutubi.pulse.master.upgrade.UpgradeManager;
import com.zutubi.pulse.master.util.monitor.Monitor;
import com.zutubi.pulse.servercore.ShutdownManager;
import com.zutubi.pulse.servercore.bootstrap.*;
import com.zutubi.pulse.servercore.util.logging.LogConfigurationManager;
import com.zutubi.tove.config.*;
import com.zutubi.tove.config.health.ConfigurationHealthChecker;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.type.record.DelegatingHandleAllocator;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.io.PropertiesWriter;
import com.zutubi.util.logging.Logger;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;

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
    private ShutdownManager shutdownManager;
    private LicenseManager licenseManager;
    private ThreadFactory threadFactory;

    /**
     * Note this is not available until we {@link #initialiseConfigurationPersistence()}.
     */
    private ConfigurationTemplateManager configurationTemplateManager;

    /**
     * Contexts for Stage A: the config subsystem.
     */
    private List<String> configContexts = new ArrayList<>();

    private List<String> migrationContext = new ArrayList<>();

    /**
     * Contexts for Stage B: database
     */
    private List<String> dataContextsA = new ArrayList<>();
    private List<String> dataContextsB = new ArrayList<>();

    /**
     * Contexts for Stage C: restore
     */
    private List<String> restoreContexts = new ArrayList<>();

    private List<String> licenseContexts = new ArrayList<>();

    /**
     * Contexts for Stage D: the upgrade system.
     */
    private List<String> upgradeContexts = new ArrayList<>();

    /**
     * Contexts for Stage E: setup / configuration.
     */
    private List<String> setupContexts = new ArrayList<>();

    /**
     * Contexts for Stage F: application startup.
     */
    private List<String> startupContexts = new ArrayList<>();

    private SetupState state = SetupState.WAITING;
    private String statusMessage;

    private boolean promptShown = false;
    private DatabaseConsole databaseConsole;

    private ProcessSetupStartupTask setupCallback;
    // Note that this is null until part way through startup
    private ConfigurationProvider configurationProvider;

    @Override
    public SetupState getCurrentState()
    {
        return state;
    }

    @Override
    public String getStatusMessage()
    {
        return statusMessage;
    }

    public void init(ProcessSetupStartupTask processSetupStartupTask)
    {
        try
        {
            this.setupCallback = processSetupStartupTask;

            statusMessage("System initializing...");
            state = SetupState.WAITING;

            // record the startup configuration so that it can be reused next time.
            createExternalConfigFileIfRequired();

            initialiseConfigurationSystem();
        }
        catch (Exception e)
        {
            setupCallback.finaliseSetup(e);
        }
    }

    public void startSetupWorkflow()
    {
        try
        {
            if (isDataRequired())
            {
                statusMessage("No data path configured, requesting via web UI...");

                // request data input.
                state = SetupState.DATA;
                showPrompt();
                return;
            }
            requestDataComplete();
        }
        catch (Exception e)
        {
            setupCallback.finaliseSetup(e);
        }
    }

    private void createExternalConfigFileIfRequired()
    {
        try
        {
            // If the user configuration file does not exist, create it now.
            EnvConfig envConfig = configurationManager.getEnvConfig();
            SystemConfiguration sysConfig = configurationManager.getSystemConfig();

            String externalConfig = envConfig.getPulseConfig();
            if (!StringUtils.stringSet(externalConfig))
            {
                // default is something like ~/.pulse2/config.properties
                externalConfig = envConfig.getDefaultPulseConfig(MasterConfigurationManager.CONFIG_DIR);
            }
            File configFile = new File(externalConfig);
            if (!configFile.isAbsolute())
            {
                configFile = configFile.getCanonicalFile();
            }

            statusMessage("Using config file '%s'.", configFile.getAbsolutePath());
            if (!configFile.isFile())
            {
                statusMessage("Config file does not exist, creating a default one.");

                // copy the template file into the config location.
                SystemPaths paths = configurationManager.getSystemPaths();
                File configTemplate = new File(paths.getConfigRoot(), "config.properties.template");
                IOUtils.copyTemplate(configTemplate, configFile);

                // write the default configuration to this template file.
                // There needs to be a way to do this without duplicating the default configuration data.
                // Unfortunately, the defaults are currently hidden by any system properties that over ride them...
                Properties props = new Properties();
                props.setProperty(SystemConfiguration.CONTEXT_PATH, SystemConfiguration.DEFAULT_CONTEXT_PATH);
                props.setProperty(SystemConfiguration.WEBAPP_PORT, SystemConfiguration.DEFAULT_WEBAPP_PORT.toString());
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

        MasterConfigurationRegistry configurationRegistry = SpringComponentContext.getBean("configurationRegistry");
        configurationRegistry.initSetup();
    }

    @Override
    public SetupDataConfiguration getDefaultData() throws IOException
    {
        String data;
        SetupDataConfiguration config = new SetupDataConfiguration();
        String userHome = System.getProperty(EnvConfig.USER_HOME);
        if (StringUtils.stringSet(userHome))
        {
            String userConfig = configurationManager.getEnvConfig().getDefaultPulseConfigDir(MasterConfigurationManager.CONFIG_DIR);
            data = FileSystemUtils.composeFilename(userConfig, "data");
        }
        else
        {
            data = "data";
        }

        config.setData(FileSystemUtils.normaliseSeparators(new File(data).getCanonicalPath()));
        return config;
    }

    @Override
    public void setData(SetupDataConfiguration data) throws IOException
    {
        File home = new File(data.getData());
        configurationManager.setPulseData(home);
        requestDataComplete();
    }

    private void requestDataComplete()
    {
        try
        {
            // If this is the first time this directory is being used as a data directory, then we need
            // to ensure that it is initialised. If we are working with an already existing directory,
            // then it will have been initialised and no re-initialisation is required (or allowed).
            Data d = configurationManager.getData();
            statusMessage("Using data path '%s'.", d.getData().getAbsolutePath());
            if (!d.isInitialised())
            {
                statusMessage("Empty data directory, initialising...");
                configurationManager.getData().init(configurationManager.getSystemPaths());
                statusMessage("Data directory initialised.");
            }

            eventManager.publish(new DataDirectoryLocatedEvent(this));

            configurationManager.loadSystemProperties();
            handleDbSetup();
        }
        catch (Exception e)
        {
            setupCallback.finaliseSetup(e);
        }
    }

    private void handleDbSetup()
    {
        ensureDriversRegisteredAndLoaded();
        
        File databaseConfig = new File(configurationManager.getData().getUserConfigRoot(), "database.properties");
        if (!databaseConfig.exists())
        {
            statusMessage("No database setup, requesting details via web UI...");
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
            statusMessage("Database migration requested, requesting details via web UI.");
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

    @Override
    public void setDatabaseType(SetupDatabaseTypeConfiguration db) throws IOException
    {
        Data data = configurationManager.getData();
        if (!db.getType().isEmbedded())
        {
            DriverRegistry registry = configurationManager.getDriverRegistry();

            if (db.getDriverFile() != null)
            {
                File driverFile = new File(db.getDriverFile());
                registry.register(db.getType().getJDBCClassName(db), driverFile);
            }
        }

        File databaseConfig = new File(data.getUserConfigRoot(), "database.properties");
        Properties p = db.getDatabaseProperties();

        IOUtils.write(p, databaseConfig, "Generated by Pulse setup wizard");
        requestDbComplete();
    }

    @Override
    public void executeMigrate(final MigrateDatabaseTypeConfiguration config)
    {
        if (state == SetupState.MIGRATE)
        {
            Monitor monitor = migrationManager.getMonitor();
            if (monitor == null || !monitor.isStarted())
            {
                threadFactory.newThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            if (!config.getType().isEmbedded())
                            {
                                if (TextUtils.stringSet(config.getDriverFile()))
                                {
                                    // install the driver... this should be handled elsewhere...
                                    DriverRegistry driverRegistry = configurationManager.getDriverRegistry();

                                    File driverFile = new File(config.getDriverFile());
                                    driverRegistry.register(config.getDriver(), driverFile);
                                }
                            }

                            Properties props = config.getDatabaseProperties();
                            migrationManager.scheduleMigration(props);
                            migrationManager.runMigration();
                        }
                        catch (IOException e)
                        {
                            LOG.severe(e);
                        }
                    }
                }).start();

                while (monitor == null || !monitor.isStarted())
                {
                    try
                    {
                        Thread.sleep(200);
                        monitor = migrationManager.getMonitor();
                    }
                    catch (InterruptedException e)
                    {
                        // Ignore.
                    }
                }
            }
        }
    }

    @Override
    public void abortMigrate()
    {
        migrationManager.cancelMigration();
        requestDbComplete();
    }

    @Override
    public void postMigrate()
    {
        requestDbComplete();
    }

    private void requestDbComplete()
    {
        statusMessage("Initializing data subsystem...");
        state = SetupState.WAITING;

        threadFactory.newThread(new Runnable()
        {
            @Override
            public void run()
            {
                SecurityUtils.loginAsSystem();
                try
                {
                    loadContexts(dataContextsA);
                    loadContexts(dataContextsB);

                    // create the database based on the hibernate configuration.
                    databaseConsole = SpringComponentContext.getBean("databaseConsole");
                    if (databaseConsole.isEmbedded())
                    {
                        statusMessage("Using embedded database (only recommended for evaluation purposes).");
                    }
                    else
                    {
                        statusMessage("Using external database '%s'.", databaseConsole.getConfig().getUrl());
                    }

                    if (!databaseConsole.schemaExists())
                    {
                        statusMessage("Database schema does not exist, initialising...");
                        try
                        {
                            databaseConsole.createSchema();
                        }
                        catch (SQLException e)
                        {
                            throw new StartupException("Failed to create the database schema. Cause: " + e.getMessage());
                        }
                        statusMessage("Database initialised.");
                    }

                    handleRestorationProcess();
                }
                catch (Exception e)
                {
                    setupCallback.finaliseSetup(e);
                }
                finally
                {
                    SecurityUtils.logout();
                }
            }
        }).start();
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
                if (StringUtils.stringSet(driverClassName) && !driverRegistry.isRegistered(driverClassName))
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

    public void requestRestoreComplete()
    {
        try
        {
            linkUserTemplates();

            initialiseConfigurationPersistence();

            loadContexts(licenseContexts);

            statusMessage("Checking license...");
            if (isLicenseRequired())
            {
                statusMessage("No valid license found, requesting via web UI...");
                //TODO: we need to provide some feedback to the user about what / why there current license
                //TODO: (if one exists) is not sufficient.
                state = SetupState.LICENSE;
                showPrompt();
                return;
            }
            requestLicenseComplete();
        }
        catch (Exception e)
        {
            setupCallback.finaliseSetup(e);
        }
    }

    private void linkUserTemplates()
    {
        File userTemplateRoot = configurationManager.getUserPaths().getUserTemplateRoot();
        if (userTemplateRoot.isDirectory())
        {
            freemarker.template.Configuration freemarkerConfiguration = SpringComponentContext.getBean("freemarkerConfiguration");
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

    @Override
    public void setLicense(final SetupLicenseConfiguration license)
    {
        SecurityUtils.runAsSystem(new Runnable()
        {
            @Override
            public void run()
            {
                String licenseKey = license.getLicense().replaceAll("\n", "");
                licenseManager.installLicense(licenseKey);
                requestLicenseComplete();
            }
        });
    }

    private void requestLicenseComplete()
    {
        try
        {
            statusMessage("License accepted.");

            // License is allowed to run this version of pulse. Therefore, it is okay to go ahead with an upgrade.
            databaseConsole.postSchemaHook();
            loadContexts(upgradeContexts);

            if (isUpgradeRequired())
            {
                statusMessage("Upgrade is required: existing data version '" + configurationManager.getData().getVersion().getVersionNumber() + "', Pulse version '" + Version.getVersion().getVersionNumber() + "'...");
                upgradeManager.prepareUpgrade();
                state = SetupState.UPGRADE;
                showPrompt();
                return;
            }

            updateVersionIfNecessary();

            requestUpgradeComplete(false);
        }
        catch (Exception e)
        {
            setupCallback.finaliseSetup(e);
        }
    }

    private void initialiseConfigurationPersistence()
    {
        RecordManager recordManager = SpringComponentContext.getBean("recordManager");
        PluginManager pluginManager = SpringComponentContext.getBean("pluginManager");
        DelegatingHandleAllocator handleAllocator = SpringComponentContext.getBean("handleAllocator");
        ConfigurationPersistenceManager configurationPersistenceManager = SpringComponentContext.getBean("configurationPersistenceManager");
        ConfigurationReferenceManager configurationReferenceManager = SpringComponentContext.getBean("configurationReferenceManager");
        configurationTemplateManager = SpringComponentContext.getBean("configurationTemplateManager");
        ConfigurationRefactoringManager configurationRefactoringManager = SpringComponentContext.getBean("configurationRefactoringManager");
        MasterConfigurationRegistry configurationRegistry = SpringComponentContext.getBean("configurationRegistry");
        ConfigurationExtensionManager configurationExtensionManager = SpringComponentContext.getBean("configurationExtensionManager");
        ConfigurationStateManager configurationStateManager = SpringComponentContext.getBean("configurationStateManager");
        ConfigurationHealthChecker configurationHealthChecker = SpringComponentContext.getBean("configurationHealthChecker");
        ConfigurationArchiver configurationArchiver = SpringComponentContext.getBean("configurationArchiver");

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

        configurationTemplateManager.setWireService(new WireService()
        {
            public void wire(Object obj)
            {
                SpringComponentContext.autowire(obj);
            }
        });
        configurationTemplateManager.init();
        
        configurationHealthChecker.setRecordManager(recordManager);
        configurationArchiver.setRecordManager(recordManager);

        LogConfigurationManager logConfigurationManager = SpringComponentContext.getBean("logConfigurationManager");
        logConfigurationManager.applyConfig();
    }

    @Override
    public void executeUpgrade()
    {
        if (state == SetupState.UPGRADE)
        {
            Monitor monitor = upgradeManager.getMonitor();
            if (monitor == null || !monitor.isStarted())
            {
                threadFactory.newThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        upgradeManager.executeUpgrade();
                    }
                }).start();

                while (monitor == null || !monitor.isStarted())
                {
                    try
                    {
                        Thread.sleep(200);
                        monitor = upgradeManager.getMonitor();
                    }
                    catch (InterruptedException e)
                    {
                        // Ignore.
                    }
                }
            }
        }
    }

    @Override
    public void postUpgrade()
    {
        if (state == SetupState.UPGRADE)
        {
            Monitor monitor = upgradeManager.getMonitor();
            if (monitor != null && monitor.isSuccessful())
            {
                requestUpgradeComplete(true);
            }
        }
    }

    public void requestUpgradeComplete(final boolean changes)
    {
        try
        {
            SecurityUtils.runAsSystem(new Runnable()
            {
                public void run()
                {
                    if (changes)
                    {
                        statusMessage("Upgrade complete.");
                    }
                    databaseConsole.postUpgradeHook(changes);

                    // Remove the upgrade context from the ComponentContext stack / namespace.
                    // They are no longer required.
                    SpringComponentContext.pop();
                    loadContexts(setupContexts);

                    if (isAdminRequired())
                    {
                        statusMessage("No admin configured, requesting via UI...");
                        state = SetupState.ADMIN;
                        showPrompt();
                        return;
                    }
                    requestAdminComplete();
                }
            });
        }
        catch (Exception e)
        {
            setupCallback.finaliseSetup(e);
        }
    }

    @Override
    public UserConfiguration setAdminUser(final AdminUserConfiguration admin) throws Exception
    {
        return SecurityUtils.callAsSystem(new Callable<UserConfiguration>()
        {
            @Override
            public UserConfiguration call()
            {
                UserConfiguration adminUser = new UserConfiguration();
                adminUser.setPermanent(true);
                adminUser.setLogin(admin.getLogin());
                adminUser.setName(admin.getName());
                adminUser.setPassword(new Md5PasswordEncoder().encodePassword(admin.getPassword(), null));
                adminUser.addDirectAuthority(ServerPermission.ADMINISTER.toString());
                if (StringUtils.stringSet(admin.getEmailAddress()))
                {
                    EmailContactConfiguration emailContact = new EmailContactConfiguration(UserConfigurationCreator.CONTACT_NAME, admin.getEmailAddress());
                    emailContact.setPrimary(true);
                    adminUser.getPreferences().addContact(emailContact);
                }
                configurationTemplateManager.insertInstance(MasterConfigurationRegistry.USERS_SCOPE, adminUser);

                // Special all-users group.
                BuiltinGroupConfiguration allUsersGroup = new BuiltinGroupConfiguration(UserManager.ALL_USERS_GROUP_NAME, Role.USER);
                allUsersGroup.setPermanent(true);
                configurationTemplateManager.insertInstance(MasterConfigurationRegistry.GROUPS_SCOPE, allUsersGroup);

                // Special anonymous users group.
                BuiltinGroupConfiguration anonymousUsersGroup = new BuiltinGroupConfiguration(UserManager.ANONYMOUS_USERS_GROUP_NAME, Role.GUEST);
                anonymousUsersGroup.setPermanent(true);
                configurationTemplateManager.insertInstance(MasterConfigurationRegistry.GROUPS_SCOPE, anonymousUsersGroup);

                // create an administrators group (for convenience)
                UserGroupConfiguration adminGroup = new UserGroupConfiguration(UserManager.ADMINS_GROUP_NAME);
                adminGroup.addServerPermission(ServerPermission.ADMINISTER);
                adminGroup.addServerPermission(ServerPermission.PERSONAL_BUILD);
                configurationTemplateManager.insertInstance(MasterConfigurationRegistry.GROUPS_SCOPE, adminGroup);

                // and a project admins group that has admin access to all projects
                UserGroupConfiguration projectAdmins = new UserGroupConfiguration(UserManager.PROJECT_ADMINS_GROUP_NAME);
                projectAdmins.addServerPermission(ServerPermission.PERSONAL_BUILD);
                projectAdmins.addServerPermission(ServerPermission.CREATE_PROJECT);
                projectAdmins.addServerPermission(ServerPermission.DELETE_PROJECT);
                configurationTemplateManager.insertInstance(MasterConfigurationRegistry.GROUPS_SCOPE, projectAdmins);

                // and a developers group that has personal build access (for convenience)
                UserGroupConfiguration developersGroup = new UserGroupConfiguration(UserManager.DEVELOPERS_GROUP_NAME);
                developersGroup.addServerPermission(ServerPermission.PERSONAL_BUILD);
                configurationTemplateManager.insertInstance(MasterConfigurationRegistry.GROUPS_SCOPE, developersGroup);

                statusMessage("Admin user and default groups created.");
                requestAdminComplete();
                return adminUser;
            }
        });
    }

    private void requestAdminComplete()
    {
        try
        {
            if (isSettingsRequired())
            {
                statusMessage("Requesting initial settings via UI...");
                state = SetupState.SETTINGS;
                initialInstallation = true;
                showPrompt();
                return;
            }

            requestSetupComplete();
        }
        catch (Exception e)
        {
            setupCallback.finaliseSetup(e);
        }

    }

    @Override
    public ServerSettingsConfiguration getDefaultServerSettings()
    {
        ServerSettingsConfiguration settings = new ServerSettingsConfiguration();
        SystemConfigurationSupport systemConfig = (SystemConfigurationSupport) configurationManager.getSystemConfig();
        settings.setBaseUrl(systemConfig.getHostUrl());
        return settings;
    }

    @Override
    public void setServerSettings(final ServerSettingsConfiguration settings) throws Exception
    {
        SecurityUtils.callAsSystem(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                GlobalConfiguration global = new GlobalConfiguration();
                global.setConfigurationPath(GlobalConfiguration.SCOPE_NAME);
                global.setPermanent(true);
                global.setBaseUrl(settings.getBaseUrl());
                global.setMasterHost(getMasterHost(settings.getBaseUrl()));

                EmailConfiguration email = global.getEmail();
                CompositeType emailType = configurationTemplateManager.getType(PathUtils.getPath(GlobalConfiguration.SCOPE_NAME, "email"), CompositeType.class);
                for (String propertyName : emailType.getSimplePropertyNames())
                {
                    TypeProperty property = emailType.getProperty(propertyName);
                    property.setValue(email, property.getValue(settings));
                }

                configurationTemplateManager.save(global);
                return null;
            }
        });

        statusMessage("Setup complete.");
        requestSetupComplete();
    }

    private String getMasterHost(String baseUrl)
    {
        String masterHost = null;
        if (baseUrl != null)
        {
            // Pull out just the host part
            try
            {
                URL url = new URL(baseUrl);
                masterHost = url.getHost();
            }
            catch (MalformedURLException e)
            {
                // Nice try
            }
        }

        if (masterHost == null)
        {
            // So much for that plan...let's try and get the host name
            try
            {
                InetAddress address = InetAddress.getLocalHost();
                masterHost = address.getCanonicalHostName();
            }
            catch (UnknownHostException e)
            {
                // Oh well, we tried
                masterHost = "localhost";
            }
        }

        return masterHost;
    }

    private void requestSetupComplete()
    {
        state = SetupState.STARTING;

        threadFactory.newThread(new Runnable()
        {
            @Override
            public void run()
            {
                SecurityUtils.loginAsSystem();
                try
                {
                    // Load the remaining contexts.  Note that the subsystems created
                    // within should generally wait for the configuration system to
                    // start (by listening for an appropriate event).
                    loadContexts(startupContexts);

                    // Fire up the extension managers.  These need most systems available
                    // (e.g. the command extension manager requires the file loader), but
                    // must come before the final init of the configuration as they are
                    // required when instantiating config objects.
                    PluginManager pluginManager = SpringComponentContext.getBean("pluginManager");
                    pluginManager.initialiseExtensions();

                    DefaultConfigurationProvider configurationProvider = SpringComponentContext.getBean("configurationProvider");
                    configurationProvider.init();
                    DefaultSetupManager.this.configurationProvider = configurationProvider;

                    setupCallback.finaliseSetup(null);
                }
                catch (Exception e)
                {
                    setupCallback.finaliseSetup(e);
                }
                finally
                {
                    SecurityUtils.logout();
                }
            }
        }).start();
    }

    private void loadContexts(List<String> contexts)
    {
        SpringComponentContext.addClassPathContextDefinitions(contexts.toArray(new String[contexts.size()]));
        SpringComponentContext.autowire(this);

        // xwork object factory refresh - need to ensure that it has a reference to the latest spring context.
        SpringObjectFactory objFact = SpringComponentContext.getOptionalBean("xworkObjectFactory");
        if (objFact != null)
        {
            objFact.setApplicationContext(SpringComponentContext.getContext());
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

    private boolean isAdminRequired()
    {
        ConfigurationTemplateManager configurationTemplateManager = SpringComponentContext.getBean("configurationTemplateManager");
        return configurationTemplateManager.getRecord(MasterConfigurationRegistry.USERS_SCOPE).size() == 0;
    }

    private boolean isSettingsRequired()
    {
        ConfigurationTemplateManager configurationTemplateManager = SpringComponentContext.getBean("configurationTemplateManager");
        return !configurationTemplateManager.getRecord(GlobalConfiguration.SCOPE_NAME).isPermanent();
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

    public void statusMessage(String format, Object... args)
    {
        statusMessage = String.format(format, args);
        System.err.println(dateStamp() + statusMessage);
    }

    public static String dateStamp()
    {
        return "[" + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(new Date()) + "] ";
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

            if (!StringUtils.stringSet(baseUrl))
            {
                // fall back to the default host url.
                SystemConfigurationSupport systemConfig = (SystemConfigurationSupport) configurationManager.getSystemConfig();
                baseUrl = systemConfig.getHostUrl();
            }

            // WARNING: sometimes calculating the host url and sometimes using a configured setting (from a previous
            //          installation or possibly a restore) results in possibly inconsistent behaviour.  It may be
            //          worth while always using the calculated host url since this is being printed on the local host.

            System.err.printf(dateStamp() + "Now go to %s and follow the prompts.\n", baseUrl);
            promptShown = true;
        }
    }

    //---( workflow for the restoration process.  Should probably shift this out of the manager...,
    // all this manager is interested in are co-ordinating the various steps, not the workflow of each step.  )---

    private RestoreManager restoreManager;

    private boolean isRestoreRequested()
    {
        // check for the existance of a PULSE_DATA/restore/archive.zip
        File archive = getArchiveFile();
        return archive != null;
    }

    /**
     * The archive file is any zip file located in the PULSE_DATA/restore directory.  It is required that only
     * one zip file be present.
     *
     * @return the zip file containing the archive.
     */
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

    private void handleRestorationProcess()
    {
        loadContexts(restoreContexts);

        if (isRestoreRequested())
        {
            File archive = getArchiveFile();
            try
            {
                statusMessage("Restoring from archive file: " + archive.getCanonicalPath());
            }
            catch (IOException e)
            {
                statusMessage("Restoring from archive file: " + archive.getAbsolutePath());
            }

            if (!archive.exists() || archive.length() == 0)
            {
                statusMessage("Specified restore archive file " + archive.getAbsolutePath() + " does not exist or is blank.");
                // shutdown, the archive is invalid.
                shutdownManager.shutdown(true, true);
                return;
            }

            try
            {
                restoreManager.prepareRestore(archive);

                // show restoration preview page.
                state = SetupState.RESTORE;
                showPrompt();
                return;
            }
            catch (ArchiveException e)
            {
                LOG.severe("Restore preparation failed: " + e.getMessage() + ", skipping restoration.", e);
            }
        }

        requestRestoreComplete();
    }

    @Override
    public void executeRestore()
    {
        if (state == SetupState.RESTORE)
        {
            Monitor monitor = restoreManager.getMonitor();
            if (!monitor.isStarted())
            {
                threadFactory.newThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            restoreManager.restoreArchive();
                        }
                        catch (ArchiveException e)
                        {
                            LOG.severe(e);
                        }
                    }
                }).start();

                while (!monitor.isStarted())
                {
                    try
                    {
                        Thread.sleep(200);
                    }
                    catch (InterruptedException e)
                    {
                        // Ignore.
                    }
                }
            }
        }
    }

    @Override
    public void abortRestore()
    {
        if (state == SetupState.RESTORE)
        {
            Monitor monitor = restoreManager.getMonitor();
            if (!monitor.isStarted())
            {
                statusMessage("Restore aborted, starting normally.");
                state = SetupState.WAITING;

                threadFactory.newThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        requestRestoreComplete();
                    }
                }).start();
            }
        }
    }

    @Override
    public void postRestore()
    {
        if (state == SetupState.RESTORE)
        {
            Monitor monitor = restoreManager.getMonitor();
            if (monitor.isFinished())
            {
                statusMessage("Restore complete.");
                state = SetupState.WAITING;

                threadFactory.newThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        requestRestoreComplete();
                    }
                }).start();
            }
        }
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

    public void setDataContextsA(List<String> dataContexts)
    {
        this.dataContextsA = dataContexts;
    }

    public void setDataContextsB(List<String> dataContexts)
    {
        this.dataContextsB = dataContexts;
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

    public void setUpgradeContexts(List<String> upgradeContexts)
    {
        this.upgradeContexts = upgradeContexts;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setRestoreManager(RestoreManager restoreManager)
    {
        this.restoreManager = restoreManager;
    }

    public void setMigrationContext(List<String> migrationContext)
    {
        this.migrationContext = migrationContext;
    }

    public void setMigrationManager(MigrationManager migrationManager)
    {
        this.migrationManager = migrationManager;
    }

    public void setShutdownManager(ShutdownManager shutdownManager)
    {
        this.shutdownManager = shutdownManager;
    }

    public void setLicenseManager(LicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }
}
