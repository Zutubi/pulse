package com.cinnamonbob.bootstrap;

import com.cinnamonbob.core.ObjectFactory;
import com.cinnamonbob.events.EventManager;
import com.cinnamonbob.events.system.SystemStartedEvent;
import com.cinnamonbob.util.logging.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultStartupManager implements StartupManager
{
    private static final Logger LOG = Logger.getLogger(DefaultStartupManager.class);

    /**
     * The systems configuration manager.
     */
    private ConfigurationManager configurationManager;

    /**
     * The systems object factory.
     */
    private ObjectFactory objectFactory;

    /**
     * Static value indicating that the startup manager has not started yet.
     */
    private static final long NOT_STARTED = -1;

    /**
     * The startup managers startup time.
     */
    private long startTime = NOT_STARTED;

    /**
     * A boolean value indicating whether or not the startup manager is currently in the
     * process of startup.
     */
    private boolean starting = false;

    private List<String> configContexts;

    private List<String> appContexts;

    private List<String> coreContexts;

    private List<String> startupRunnables = new LinkedList<String>();

    /**
     * Required resource.
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
     * @param objectFactory
     */
    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    /**
     * Specify the spring contexts that need to be loaded to initialise the configuration
     * mode of this system.
     *
     * @param configContexts
     */
    public void setConfigContexts(List<String> configContexts)
    {
        this.configContexts = configContexts;
    }

    /**
     * Specify the spring contexts that need to be loaded to initialise the main system.
     *
     * @param appContexts
     */
    public void setAppContexts(List<String> appContexts)
    {
        this.appContexts = appContexts;
    }

    /**
     * Specify the spring contexts that make up the core of the system.
     *
     * @param coreContexts
     */
    public void setCoreContexts(List<String> coreContexts)
    {
        this.coreContexts = coreContexts;
    }

    public void setStartupRunnables(List<String> startupRunnables)
    {
        this.startupRunnables = startupRunnables;
    }

    /**
     * Entry point to starting the system.
     *
     */
    public void init() throws StartupException
    {
        if (isSystemStarted())
        {
            throw new StartupException("The system has alredy started.");
        }
        if (isSystemStarting())
        {
            throw new StartupException("The system is currently starting up.");
        }

        try
        {
            starting = true;

            // load the core context, common to all of the system configurations.
            ComponentContext.addClassPathContextDefinitions(coreContexts.toArray(new String[coreContexts.size()]));

            if (configurationManager.requiresSetup())
            {
                startConfiguration();
            }
            else
            {
                startApplication();
            }
        }
        catch (Exception e)
        {
            throw new StartupException(e);
        }
    }

    public void continueStartup() throws Exception
    {
        // somehow we need to unload the setup spring context to ensure that its clear what is where.
        // this includes shutting down the database connections if necessary....
        // make sure that we dont 'unload/shutdown' the web server.

        // now we start the application proper.
        startApplication();
    }

    /**
     * Start the configuration context.
     *
     * @throws Exception
     */
    private void startConfiguration() throws Exception
    {
        // startup the web server.
        WebManager webManager = (WebManager) ComponentContext.getBean("webManager");

        // load the application context. we do not expect this to take long, so we dont worry about
        // a holding page here.
        ComponentContext.addClassPathContextDefinitions(configContexts.toArray(new String[configContexts.size()]));

        // i) set the system starting pages (periodically refresh)
        webManager.deploySetup();

        // let the user know that they should continue / complete the setup process via the Web UI.
        int serverPort = configurationManager.getAppConfig().getServerPort();
        System.err.println("Now go to http://localhost:"+serverPort+" to complete the setup.");
    }

    /**
     * Start the main application context.
     *
     */
    public void startApplication()
    {
        // loading here will take some time. So, we need to provide some feedback to the
        // user about what is going on. We do this by loading a temporary webapp that contains a startup ui.

        // startup the web server.
        WebManager webManager = (WebManager) ComponentContext.getBean("webManager");

        // i) set the system starting pages (periodically refresh)
        webManager.deployStartup();

        // load the application context.
        ComponentContext.addClassPathContextDefinitions(appContexts.toArray(new String[appContexts.size()]));

        // application contexts have finished loading
        // i) run startup tasks
        runStartupTasks();

        // ii) time to deploy the may application.

        webManager.deployMain();

        // record the start time
        startTime = System.currentTimeMillis();

        // send a message to the rest of the system that all is good to go.
        EventManager eventManager = (EventManager) ComponentContext.getBean("eventManager");
        eventManager.publish(new SystemStartedEvent(this));

        // let the user know that the system is now up and running.
        int serverPort = configurationManager.getAppConfig().getServerPort();
        System.err.println("The server is now available at http://localhost:"+serverPort+".");
    }

    private void runStartupTasks()
    {
        for (String name : startupRunnables)
        {
            try
            {
                Runnable instance = objectFactory.buildBean(name);
                instance.run();
            }
            catch (Exception e)
            {
                LOG.warning("Failed to run startup task "+name+". Reason: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Indicates whether or not the system is currently starting up.
     *
     * @return true if the system is starting, false otherwise.
     */
    public boolean isSystemStarting()
    {
        return starting;
    }

    /**
     * Indicates whether or not the system has started.
     *
     * @return true if the system has started, false otherwise.
     */
    public boolean isSystemStarted()
    {
        return startTime != NOT_STARTED;
    }

    /**
     * Returns the amount time (in milliseconds) since the system started.
     *
     * @return the uptime in milliseconds, or 0 if the system has not yet started.
     */
    public long getUptime()
    {
        if (isSystemStarted())
        {
            return System.currentTimeMillis() - startTime;
        }
        return 0;
    }

    /**
     * Returns the system start time in milliseconds, or -1 if the system has not
     * yet started.
     *
     * @return start time in milliseconds.
     */
    public long getStartTime()
    {
        return startTime;
    }
}
