package com.zutubi.pulse.bootstrap;

import com.opensymphony.util.TextUtils;
import com.opensymphony.xwork.spring.SpringObjectFactory;
import com.zutubi.pulse.bootstrap.conf.EnvConfig;
import com.zutubi.pulse.config.PropertiesWriter;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.upgrade.UpgradeManager;
import com.zutubi.pulse.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class DefaultSetupManager implements SetupManager
{
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
            System.err.println("Now go to " + baseUrl + " and follow the prompts.");
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
        File f = new File(externalConfig);
        if (!f.isAbsolute())
        {
            f = f.getCanonicalFile();
        }
        if (!f.isFile())
        {
            // copy the template file into the config location.
            SystemPaths paths = configurationManager.getSystemPaths();
            File configTemplate = new File(paths.getConfigRoot(), "config.properties");
            File parentFile = f.getParentFile();
/*
            System.out.println("File: " + f);
            System.out.println("ParentFile: " + parentFile);
*/
            if (!parentFile.isDirectory() && !parentFile.mkdirs())
            {
                throw new IOException("Unable to create parent directory '" + parentFile.getAbsolutePath() + "' for config file");
            }
            if (!f.createNewFile())
            {
                throw new IOException("Unable to create config file '" + f.getAbsolutePath() + "'");
            }
            IOUtils.copyFile(configTemplate, f);

            // write the default configuration to this template file.
            // There needs to be a way to do this without duplicating the default configuration data.
            // Unfortunately, the defaults are currently hidden by any system properties that over ride them...
            Properties props = new Properties();
            props.setProperty(SystemConfiguration.CONTEXT_PATH, "/");
            props.setProperty(SystemConfiguration.WEBAPP_PORT, "8080");
            props.setProperty(SystemConfiguration.PULSE_DATA, (sysConfig.getDataPath() != null ? sysConfig.getDataPath() : ""));

            PropertiesWriter writer = new PropertiesWriter();
            writer.write(f, props);
        }
    }

    public void requestDataComplete() throws IOException
    {
        // If this is the first time this directory is being used as a data directory, then we need
        // to ensure that it is initialised. If we are working with an already existing directory,
        // then it will have been initialised and no re-initialisation is required (or allowed).
        Data d = configurationManager.getData();
        if (!d.isInitialised())
        {
            configurationManager.getData().init(configurationManager.getSystemPaths());
        }

        state = SetupState.STARTING;
        if (isLicenseRequired())
        {
            //TODO: we need to provide some feedback to the user about what / why there current license
            //TODO: if one exists is not sufficient.
            state = SetupState.LICENSE;
            showPrompt();
            return;
        }
        requestLicenseComplete();
    }

    public void requestLicenseComplete()
    {
        state = SetupState.STARTING;

        // load db contexts...
        loadContexts(daoContexts);

        // create the database based on the hibernate configuration.
        DatabaseBootstrap dbBootstrap = (DatabaseBootstrap) ComponentContext.getBean("databaseBootstrap");
        if (!dbBootstrap.schemaExists())
        {
            dbBootstrap.initialiseDatabase();
        }

        loadContexts(upgradeContexts);

        if (isUpgradeRequired())
        {
            state = SetupState.UPGRADE;
            showPrompt();
            return;
        }

        requestUpgradeComplete();
    }

    public void requestUpgradeComplete()
    {
        state = SetupState.STARTING;

        // load the setup contexts containing the beans required to continue the setup process.
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
        return !LicenseHolder.hasAuthorization("canRunPulse");
    }

    private boolean isUpgradeRequired()
    {
        return upgradeManager.isUpgradeRequired(configurationManager.getData());
    }

    private boolean isSetupRequired()
    {
        return userManager.getUserCount() == 0;
    }

    /**
     * Required resource.
     *
     * @param userManager
     */
    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    /**
     * Required resource.
     *
     * @param startupManager
     */
    public void setStartupManager(StartupManager startupManager)
    {
        this.startupManager = startupManager;
    }

    /**
     * Required resource.
     *
     * @param upgradeManager
     */
    public void setUpgradeManager(UpgradeManager upgradeManager)
    {
        this.upgradeManager = upgradeManager;
    }

    /**
     * Required resources.
     *
     * @param configurationManager
     */
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
