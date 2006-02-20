package com.cinnamonbob.bootstrap;

import com.cinnamonbob.Version;
import com.cinnamonbob.spring.DelegatingApplicationContext;
import com.cinnamonbob.core.util.Constants;
import com.cinnamonbob.util.logging.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.lang.reflect.Constructor;

/**
 * The startup manager triggered by the bootstrap context. This startup manager is
 * responsible for coordinating the application startup by triggering the configured
 * startup managers.
 */
public class BootstrapStartupManager implements StartupManager, ApplicationContextAware
{
    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(BootstrapStartupManager.class);

    /**
     * The system contexts that define the various systems that require starting.
     */
    private List<String> systemContexts;

    private List<String> baseContexts;

    private List<String> startupRunnables;

    private final Object lock = new Object();

    private boolean startupTriggered;

    /**
     * The bootstrap context, contains reference to all of the bootstrap resources.
     */
    private ApplicationContext bootstrapContext;

    /**
     * The systemStarted variable indicates whether or not the system has started.
     */
    private boolean systemStarted;

    /**
     * The systems start time.
     */
    private long startTime;

    public void startup() throws StartupException
    {
        // should never run the system startup more then once.
        synchronized (lock)
        {
            if (startupTriggered)
            {
                throw new StartupException("Startup can not be triggered more then once.");
            }
            startupTriggered = true;
        }

        ComponentContext.setContext((ConfigurableApplicationContext) bootstrapContext);
        for (final String baseContext : baseContexts)
        {
            ComponentContext.addClassPathContextDefinitions(new String[]{baseContext});
        }

        ConfigurableApplicationContext baseContext = (ConfigurableApplicationContext)
                ((DelegatingApplicationContext) ComponentContext.getContext()).getDelegate();

        for (final String systemContext : systemContexts)
        {
            // load the base system context.
            ComponentContext.setContext(baseContext);
            ComponentContext.addClassPathContextDefinitions(new String[]{systemContext});

            // each system context requires a systemManager bean that is called to handle
            // the system initialisation process.
            final SystemStartupManager managerSystem =
                    (SystemStartupManager) ComponentContext.getBean("systemManager");

            // the callback will be triggered when the systemManager has completed its
            // startup routines.
            managerSystem.addCallback(new StartupCallback()
            {
                public void done()
                {
                    synchronized (lock)
                    {
                        lock.notifyAll();
                    }
                }
            });

            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    managerSystem.startup();
                }
            });
            t.start();

            // This callback may take some time, since it could require interaction with the
            // user or long running tasks like initialisation of the database. So we wait...
            try
            {
                if (!managerSystem.isSystemStarted())
                {
                    synchronized (lock)
                    {
                        lock.wait();
                    }
                }
                // else system startup was quick, so we dont need to wait.

            }
            catch (InterruptedException e)
            {
                LOG.error(e.getMessage());
                // why were we interrupted?... .. might be a good time to abort startup.
            }
        }

        for (String name : startupRunnables)
        {
            try
            {
                Class clazz = Class.forName(name);
                Constructor constructor = clazz.getConstructor();
                Runnable instance = (Runnable) constructor.newInstance();
                ComponentContext.autowire(instance);
                instance.run();
            }
            catch (Exception e)
            {
                LOG.warning("Failed to run startup task. Reason: " + e.getMessage(), e);
            }
        }

        // all of the systems have started. Record start time.
        setSystemStarted(true);

        logStartupMessage();
    }

    private void logStartupMessage()
    {
        StringBuffer buffer = new StringBuffer();
        String lineSeparator = Constants.LINE_SEPARATOR;
        buffer.append(lineSeparator);
        buffer.append("###################################").append(lineSeparator);
        buffer.append("##  System startup complete:").append(lineSeparator);
        buffer.append("##  ").append(Version.getVersion()).append(lineSeparator);
        buffer.append("##  ").append(Version.getBuildDate()).append(lineSeparator);
        buffer.append("##  ").append(Version.getBuildNumber()).append(lineSeparator);
        buffer.append("###################################").append(lineSeparator);
        LOG.info(buffer.toString());
    }

    /**
     * @param context
     * @throws BeansException
     */
    public void setApplicationContext(ApplicationContext context) throws BeansException
    {
        this.bootstrapContext = context;
    }

    /**
     * Retrieve the systems uptime.
     */
    public long getUptime()
    {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Retrieve the systems start time. This is the time when the system start event was generated
     * and is counted from the time at which the last system was executed.
     */
    public long getStartTime()
    {
        return startTime;
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

    /**
     * Specify the system contexts to be executed by this startup manager.
     *
     * @param contexts
     */
    public void setSystemContexts(List<String> contexts)
    {
        this.systemContexts = contexts;
    }

    public void setBaseContexts(List<String> baseContexts)
    {
        this.baseContexts = baseContexts;
    }

    public void setStartupRunnables(List<String> startupRunnables)
    {
        this.startupRunnables = startupRunnables;
    }
}
