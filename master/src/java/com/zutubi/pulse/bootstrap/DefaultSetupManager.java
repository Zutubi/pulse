package com.zutubi.pulse.bootstrap;

import com.opensymphony.xwork.spring.SpringObjectFactory;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.upgrade.UpgradeManager;

import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultSetupManager implements SetupManager
{
    private MasterConfigurationManager configurationManager;
    private StartupManager startupManager;
    private UserManager userManager;
    private UpgradeManager upgradeManager;

    private List<String> daoContexts;
    private List<String> setupContexts;
    private List<String> upgradeContexts;
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
            return;
        }
        requestDataComplete();
    }

    public void requestDataComplete()
    {
        state = SetupState.STARTING;
        if (isLicenseRequired())
        {
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
        License l = configurationManager.getData().getLicense();
        if (l != null)
        {
            //TODO: check if it is able to run the installed version.
            return false;
        }
        else
        {
            // the license is invalid / failed to decode. We need a new one before continuing.
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
