/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.upgrade.UpgradeManager;

import java.io.IOException;
import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultSetupManager implements SetupManager
{
    private ConfigurationManager configurationManager;
    private StartupManager startupManager;
    private UpgradeManager upgradeManager;

    private List<String> daoContexts;
    private List<String> setupContexts;

    public void setDaoContexts(List<String> daoContexts)
    {
        this.daoContexts = daoContexts;
    }

    public void setSetupContexts(List<String> setupContexts)
    {
        this.setupContexts = setupContexts;
    }

    /**
     * Prepare for the setup processing.
     *
     */
    public void prepareSetup()
    {
        Home home = configurationManager.getHome();
        if (home == null || home.getHome() == null)
            throw new IllegalStateException("");

        try
        {
            home.init();
        }
        catch (IOException e)
        {
            throw new StartupException("Failed to initialise home directory.");
        }

        // load database context.
        ComponentContext.addClassPathContextDefinitions(daoContexts.toArray(new String[daoContexts.size()]));

        // create the database based on the hibernate configuration.
        DatabaseBootstrap dbBootstrap = (DatabaseBootstrap) ComponentContext.getBean("databaseBootstrap");
        dbBootstrap.initialiseDatabase();

        // load the setup contexts containing the beans required to continue the setup process.
        ComponentContext.addClassPathContextDefinitions(setupContexts.toArray(new String[setupContexts.size()]));
    }

    /**
     * The setup processing is complete. We can now start the application.
     */
    public void setupComplete()
    {
        if (systemRequiresSetup() || systemRequiresUpgrade())
            throw new IllegalStateException();

        startupManager.startApplication();
    }

    /**
     * Check if the setup processing is required.
     *
     * @return true if the system requires setup, false otherwise.
     */
    public boolean systemRequiresSetup()
    {
        return !configurationManager.getHome().isInitialised();
    }

    /**
     * Check if upgrading is required.
     *
     * @return true if the system home requires an upgrade, false otherwise.
     */
    public boolean systemRequiresUpgrade()
    {
        return upgradeManager.isUpgradeRequired(configurationManager.getHome());
    }

    /**
     * Required resources.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
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
}
