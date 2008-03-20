package com.zutubi.pulse.bootstrap;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.spring.SpringObjectFactory;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.conf.EnvConfig;
import com.zutubi.pulse.config.PropertiesWriter;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.upgrade.UpgradeManager;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class DefaultSetupManager implements SetupManager
{
    private static final Logger LOG = Logger.getLogger(DefaultSetupManager.class);
    
    private MasterConfigurationManager configurationManager;
    private StartupManager startupManager;
    private UserManager userManager;
    private UpgradeManager upgradeManager;

    /**
     * Contexts for Stage A: the database.
     */
    private List<String> daoContexts;

    /**
     * Contexts for Stage B: the upgrade system.
     */
    private List<String> upgradeContexts;

    /**
     * Contexts for Stage C: setup / configuration.
     */
    private List<String> setupContexts;

    /**
     * Contexts for Stage D: application startup.
     */
    private List<String> startupContexts;

    private List<String> postStartupContexts;

    private SetupState state = SetupState.STARTING;

    private boolean promptShown = false;
    private DatabaseConsole databaseConsole;

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

            //TODO: I18N this message - note, this also only works if the user is installing on the local
            //TODO: machine. We need to provide a better (widely applicable) URL.

            String baseUrl = configurationManager.getAppConfig().getBaseUrl();
            if (!TextUtils.stringSet(baseUrl))
            {
                baseUrl = systemConfig.getHostUrl();
            }
            printConsoleMessage("Now go to %s and follow the prompts.", baseUrl);
            promptShown = true;
        }
    }

    public void startSetupWorkflow() throws IOException
    {
        state = SetupState.STARTING;

        try
        {
            createExternalConfigFileIfRequired();
        }
        catch (IOException e)
        {
            System.err.println("WARNING: " + e.getMessage());
        }

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

    public void requestDataComplete() throws IOException
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

        loadSystemProperties();

        state = SetupState.STARTING;
        printConsoleMessage("Checking license...");
        if (isLicenseRequired())
        {
            printConsoleMessage("No valid license found, requesting via web UI...");
            //TODO: we need to provide some feedback to the user about what / why there current license
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

    public void requestLicenseComplete()
    {
        printConsoleMessage("License accepted.");
        state = SetupState.STARTING;

        // License is allowed to run this version of pulse. Therefore, it is okay to go ahead with an upgrade.

        // load db contexts...
        loadContexts(daoContexts);

        // create the database based on the hibernate configuration.
        databaseConsole = (DatabaseConsole) ComponentContext.getBean("databaseConsole");
        if(databaseConsole.isEmbedded())
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

    public void requestUpgradeComplete(boolean changes)
    {
        if (changes)
        {
            printConsoleMessage("Upgrade complete.");
        }
        databaseConsole.postUpgradeHook(changes);

        state = SetupState.STARTING;

        // Remove the upgrade context from the ComponentContext stack / namespace.
        // They are no longer required.
        ComponentContext.pop();
        
        loadContexts(setupContexts);

        if (isSetupRequired())
        {
            printConsoleMessage("Database empty, requesting setup via web UI...");
            state = SetupState.SETUP;
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
        loadContexts(startupContexts);
        loadContexts(postStartupContexts);

        startupManager.continueApplicationStartup();
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

    public static void printConsoleMessage(String format, Object... args)
    {
        String date = "[" + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(new Date()) + "] ";
        System.err.printf(date + format + "\n", args);
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setStartupManager(StartupManager startupManager)
    {
        this.startupManager = startupManager;
    }

    public void setUpgradeManager(UpgradeManager upgradeManager)
    {
        this.upgradeManager = upgradeManager;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setDaoContexts(List<String> daoContexts)
    {
        this.daoContexts = daoContexts;
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
