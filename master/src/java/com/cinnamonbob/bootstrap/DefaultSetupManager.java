package com.cinnamonbob.bootstrap;

import com.cinnamonbob.util.logging.Logger;


/**
 * <class-comment/>
 */
public class DefaultSetupManager implements SetupManager
{
    private static final Logger LOG = Logger.getLogger(DefaultSetupManager.class);

    /**
     * The systems configuration manager.
     */
    private ConfigurationManager configurationManager;

    /**
     * @see com.cinnamonbob.bootstrap.SetupManager#isSetup()
     */
    public boolean isSetup()
    {
        Home home = configurationManager.getHome();
        if (home == null || !home.isInitialised())
        {
            return false;
        }
        return true;
    }

    public void setup() throws StartupException
    {
        // bob home configuration.
        Home home = configurationManager.getHome();
        if (home == null || !home.isInitialised())
        {
            doSetup();
            return;
        }

        // all systems are go, start the application.
    }

    private void doSetup()
    {
        // need to ask the user for a bob home value.
        WebUIState.startSetup();

        // log to the terminal that the system is ready. Bypass the logging framework to ensure that
        // it is always displayed.
        //TODO: i18n this string...
        int serverPort = configurationManager.getAppConfig().getServerPort();
        System.err.println("Now go to http://localhost:"+serverPort+" to complete the setup.");
    }

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(com.cinnamonbob.bootstrap.ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
