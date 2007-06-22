package com.zutubi.pulse.bootstrap;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.spring.SpringObjectFactory;
import com.zutubi.prototype.config.*;
import com.zutubi.prototype.type.record.DelegatingHandleAllocator;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.conf.EnvConfig;
import com.zutubi.pulse.config.PropertiesWriter;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.logging.LogConfigurationManager;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.upgrade.UpgradeManager;
import com.zutubi.util.IOUtils;
import com.zutubi.util.logging.Logger;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    
    private MasterConfigurationManager configurationManager;
    private UserManager userManager;
    private UpgradeManager upgradeManager;

    /**
     * Contexts for Stage A: the config subsystem.
     */
    private List<String> configContexts = new LinkedList<String>();

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

    public SetupState getCurrentState()
    {
        return state;
    }

    private void showPrompt()
    {
        if (!promptShown)
        {
            // let the user know that they should continue / complete the setup process via the Web UI.
            SystemConfigurationSupport systemConfig = (SystemConfigurationSupport) configurationManager.getSystemConfig();
            // FIXME showing the base url is tricky as this can be called
            // FIXME very early
            String baseUrl = systemConfig.getHostUrl();
            System.err.println("Now go to " + baseUrl + " and follow the prompts.");
            promptShown = true;
        }
    }

    public void startSetupWorkflow(ProcessSetupStartupTask processSetupStartupTask)
    {
        this.setupCallback = processSetupStartupTask;

        state = SetupState.STARTING;

        try
        {
            createExternalConfigFileIfRequired();
        }
        catch (IOException e)
        {
            System.err.println("WARNING: " + e.getMessage());
        }

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

    private void createExternalConfigFileIfRequired() throws IOException
    {
        // If the user configuration file does not exist, create it now.
        EnvConfig envConfig = configurationManager.getEnvConfig();
        SystemConfiguration sysConfig = configurationManager.getSystemConfig();

        String externalConfig = envConfig.getPulseConfig();
        if (!TextUtils.stringSet(externalConfig))
        {
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

        loadSystemProperties();
        linkUserTemplates();

        // load db contexts...
        loadContexts(daoContexts);
        initialiseConfigurationPersistence();
        
        loadContexts(licenseContexts);

        state = SetupState.STARTING;
        if (isLicenseRequired())
        {
            //TODO: we need to provide some feedback to the user about what / why their current license
            //TODO: if one exists is not sufficient.
            state = SetupState.LICENSE;
            showPrompt();
            return;
        }
        requestLicenseComplete();
    }

    private void loadSystemProperties()
    {
        File propFile = new File(configurationManager.getUserPaths().getUserConfigRoot(), "system.properties");
        if(propFile.exists())
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
        if(userTemplateRoot.isDirectory())
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
        DelegatingHandleAllocator handleAllocator = ComponentContext.getBean("handleAllocator");
        ConfigurationPersistenceManager configurationPersistenceManager = ComponentContext.getBean("configurationPersistenceManager");
        ConfigurationReferenceManager configurationReferenceManager = ComponentContext.getBean("configurationReferenceManager");
        ConfigurationTemplateManager configurationTemplateManager = ComponentContext.getBean("configurationTemplateManager");
        ConfigurationRegistry configurationRegistry = ComponentContext.getBean("configurationRegistry");
        DefaultConfigurationProvider configurationProvider = ComponentContext.getBean("configurationProvider");

        recordManager.init();
        handleAllocator.setDelegate(handleAllocator);
        configurationPersistenceManager.setRecordManager(recordManager);
        configurationReferenceManager.setRecordManager(recordManager);
        configurationTemplateManager.setRecordManager(recordManager);
        configurationRegistry.init();
        configurationTemplateManager.init();
        configurationProvider.init();
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
        return upgradeManager.isUpgradeRequired(configurationManager.getData());
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
}
