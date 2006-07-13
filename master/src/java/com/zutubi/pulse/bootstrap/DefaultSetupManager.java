package com.zutubi.pulse.bootstrap;

import com.opensymphony.xwork.spring.SpringObjectFactory;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.license.LicenseManager;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.upgrade.UpgradeManager;

import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultSetupManager implements SetupManager
{
    private MasterConfigurationManager configurationManager;
    private LicenseManager licenseManager;
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

    private SetupState state;

    public SetupState getCurrentState()
    {
        return state;
    }

    public void startSetupWorkflow()
    {
        state = SetupState.STARTING;
        if (isDataRequired())
        {
            // request data input.
            state = SetupState.DATA;

            // let the user know that they should continue / complete the setup process via the Web UI.
            int serverPort = configurationManager.getAppConfig().getServerPort();

            //TODO: I18N this message - note, this also only works if the user is installing on the local
            //TODO: machine. We need to provide a better (widely applicable) URL.

            System.err.println("Now go to http://localhost:"+serverPort+" and follow the prompts.");

            return;
        }
        requestDataComplete();
    }

    public void requestDataComplete()
    {
        state = SetupState.STARTING;
        if (isLicenseRequired())
        {
            //TODO: we need to provide some feedback to the user about what / why there current license
            //TODO: if one exists is not sufficient.
            state = SetupState.LICENSE;
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
        try
        {
            // if we are not licensed, then request that a license be provided.
            return !licenseManager.isLicensed();
        }
        catch (LicenseException e)
        {
            return true;
        }
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

    /**
     * Required resource.
     *
     * @param licenseManager
     */
    public void setLicenseManager(LicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
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

    public void setUpgradeContexts(List<String> upgradeContexts)
    {
        this.upgradeContexts = upgradeContexts;
    }

}
