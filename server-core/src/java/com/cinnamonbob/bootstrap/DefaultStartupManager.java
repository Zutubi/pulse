package com.cinnamonbob.bootstrap;

import com.cinnamonbob.core.ObjectFactory;
import com.cinnamonbob.events.EventManager;
import com.cinnamonbob.events.system.SystemStartedEvent;
import com.cinnamonbob.util.logging.Logger;
import com.opensymphony.xwork.config.providers.XmlConfigurationProvider;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class DefaultStartupManager implements StartupManager
{
    private static final Logger LOG = Logger.getLogger(DefaultStartupManager.class);

    private List<String> setupContexts;
    private List<String> systemContexts;
    private List<String> startupRunnables = new LinkedList<String>();

    private boolean systemStarted;
    private long startTime;

    private ObjectFactory objectFactory;
    private ConfigurationManager configurationManager;

    public void init() throws StartupException
    {
        if (isSystemStarted())
        {
            throw new StartupException("Attempt to start system when it has already started.");
        }

        try
        {
            ComponentContext.addClassPathContextDefinitions(setupContexts.toArray(new String[setupContexts.size()]));

            SetupManager setupManager = (SetupManager) ComponentContext.getBean("setupManager");
            if (!setupManager.setup())
            {
                // log to the terminal that the system is ready. Bypass the logging framework to ensure that
                // it is always displayed.
                //TODO: i18n this string...
                int serverPort = configurationManager.getAppConfig().getServerPort();
                System.err.println("Now go to http://localhost:"+serverPort+" to complete the setup.");
                return;
            }

            startApplication();
        }
        catch (Exception e)
        {
            throw new StartupException(e);
        }
    }

    public void startApplication()
    {
        try
        {
            com.opensymphony.xwork.config.ConfigurationManager.clearConfigurationProviders();
            com.opensymphony.xwork.config.ConfigurationManager.addConfigurationProvider(new XmlConfigurationProvider("xwork.xml"));
            com.opensymphony.xwork.config.ConfigurationManager.getConfiguration().reload();
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }

        ComponentContext.addClassPathContextDefinitions(systemContexts.toArray(new String[systemContexts.size()]));

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

        setSystemStarted(true);

        EventManager eventManager = (EventManager) ComponentContext.getBean("eventManager");
        eventManager.publish(new SystemStartedEvent(this));
    }

    private void setSystemStarted(boolean b)
    {
        systemStarted = b;
        if (systemStarted)
        {
            startTime = System.currentTimeMillis();
        }
    }

    public boolean isSystemStarted()
    {
        return systemStarted;
    }

    public void setSystemContexts(List<String> contexts)
    {
        this.systemContexts = contexts;
    }

    public void setSetupContexts(List<String> setupContexts)
    {
        this.setupContexts = setupContexts;
    }

    public void setStartupRunnables(List<String> classes)
    {
        this.startupRunnables = classes;
    }

    public long getUptime()
    {
        return System.currentTimeMillis() - startTime;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
