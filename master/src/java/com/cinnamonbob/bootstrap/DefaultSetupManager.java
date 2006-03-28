package com.cinnamonbob.bootstrap;

import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultSetupManager implements SetupManager
{
    private ConfigurationManager configurationManager;
    private StartupManager startupManager;

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

    public void executePostBobHomeSetup()
    {
        Home home = configurationManager.getHome();
        home.init();

        // load database context.
        ComponentContext.addClassPathContextDefinitions(daoContexts.toArray(new String[daoContexts.size()]));

        // create the database based on the hibernate configuration.
        DatabaseBootstrap dbBootstrap = (DatabaseBootstrap) ComponentContext.getBean("databaseBootstrap");
        dbBootstrap.initialiseDatabase();

        // load the setup contexts containing the beans required to continue the setup process.
        ComponentContext.addClassPathContextDefinitions(setupContexts.toArray(new String[setupContexts.size()]));
    }

    public void setupComplete() throws Exception
    {
        startupManager.startApplication();
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
}
