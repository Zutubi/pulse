package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.upgrade.UpgradeManager;

import java.io.IOException;
import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultSetupManager implements SetupManager
{
    private MasterConfigurationManager configurationManager;
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
        Data data = configurationManager.getData();
        if (data == null || data.getData() == null)
        {
            throw new IllegalStateException("");
        }

        try
        {
            data.init();
        }
        catch (IOException e)
        {
            throw new StartupException("Failed to initialise data directory.");
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
        {
            throw new IllegalStateException();
        }
        startupManager.continueApplicationStartup();
    }

    /**
     * Check if the setup processing is required.
     *
     * @return true if the system requires setup, false otherwise.
     */
    public boolean systemRequiresSetup()
    {
        return !configurationManager.getData().isInitialised();
    }

    /**
     * Check if upgrading is required.
     *
     * @return true if the system data requires an upgrade, false otherwise.
     */
    public boolean systemRequiresUpgrade()
    {
        return upgradeManager.isUpgradeRequired(configurationManager.getData());
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
