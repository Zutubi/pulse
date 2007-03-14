package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.config.ConfigSupport;
import com.zutubi.pulse.config.FileConfig;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.system.SystemStartedEvent;
import com.zutubi.pulse.freemarker.CustomFreemarkerManager;
import com.zutubi.pulse.security.AcegiSecurityManager;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * <class-comment/>
 */
public class DefaultStartupManager implements StartupManager
{
    private static final Logger LOG = Logger.getLogger(DefaultStartupManager.class);

    /**
     * The systems configuration manager.
     */
    private MasterConfigurationManager configurationManager;

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

    private List<String> coreContexts;

    private List<String> setupContexts;

    private List<String> startupRunnables = new LinkedList<String>();

    /**
     * Required resource.
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
     * @param setupContexts
     */
    public void setSetupContexts(List<String> setupContexts)
    {
        this.setupContexts = setupContexts;
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

            // record the startup config to the config directory.
            SystemConfiguration config = configurationManager.getSystemConfig();
            File configRoot = configurationManager.getSystemPaths().getConfigRoot();
            File startupConfigFile = new File(configRoot, "runtime.properties");
            ConfigSupport startupConfig = new ConfigSupport(new FileConfig(startupConfigFile));
            startupConfig.setProperty(SystemConfiguration.CONTEXT_PATH, config.getContextPath());
            startupConfig.setInteger(SystemConfiguration.WEBAPP_PORT, config.getServerPort());

            CustomFreemarkerManager.initialiseLogging();

            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
            {
                public void uncaughtException(Thread t, Throwable e)
                {
                    java.util.logging.Logger.getLogger("").log(Level.SEVERE, "Uncaught exception: " + e.getMessage(), e);
                }
            });

            startApplicationSetup();
        }
        catch (Exception e)
        {
            throw new StartupException(e);
        }
    }

    /**
     * Start the configuration context.
     *
     * @throws Exception
     */
    private void startApplicationSetup() throws Exception
    {
        // load the core context, common to all of the system configurations.
        ComponentContext.addClassPathContextDefinitions(coreContexts.toArray(new String[coreContexts.size()]));

        // load the setup context. we do not expect this to take long, so we dont worry about a holding page here.
        ComponentContext.addClassPathContextDefinitions(setupContexts.toArray(new String[setupContexts.size()]));

        // startup the web server.
        WebManager webManager = (WebManager) ComponentContext.getBean("webManager");

        // i) set the system starting pages (periodically refresh)
        webManager.deploySetup();

        SetupManager setupManager = (SetupManager) ComponentContext.getBean("setupManager");
        setupManager.startSetupWorkflow();
    }

    public void continueApplicationStartup()
    {
        loadSystemProperties();

        WebManager webManager = (WebManager) ComponentContext.getBean("webManager");

        // handle the initialisation of the security manager, since this can not be done within the spring context file.
        AcegiSecurityManager securityManager = (AcegiSecurityManager) ComponentContext.getBean("securityManager");
        securityManager.init();

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
        MasterConfiguration appConfig = configurationManager.getAppConfig();
        SystemConfiguration sysConfig = configurationManager.getSystemConfig();

        //TODO: I18N this message.
        String str = "The server is now available on port %s at context path '%s' [base URL configured as: %s]";
        String msg = String.format(str, sysConfig.getServerPort(), sysConfig.getContextPath(), appConfig.getBaseUrl());
        System.err.println(msg);
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
