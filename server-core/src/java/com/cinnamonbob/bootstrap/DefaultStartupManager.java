package com.cinnamonbob.bootstrap;

import com.cinnamonbob.events.EventManager;
import com.cinnamonbob.events.system.SystemStartedEvent;
import com.cinnamonbob.util.logging.Logger;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class DefaultStartupManager implements StartupManager
{
    private static final Logger LOG = Logger.getLogger(DefaultStartupManager.class);

    private List<String> systemContexts;
    private List<String> startupRunnables = new LinkedList<String>();

    private boolean systemStarted;
    private long startTime;

    public void init() throws StartupException
    {
        if (isSystemStarted())
        {
            throw new StartupException("Attempt to start system when it has already started.");
        }

        try
        {
            
            ComponentContext.addClassPathContextDefinitions(systemContexts.toArray(new String[systemContexts.size()]));

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

            setSystemStarted(true);

            EventManager eventManager = (EventManager) ComponentContext.getBean("eventManager");
            eventManager.publish(new SystemStartedEvent(this));
        }
        catch (Exception e)
        {
            throw new StartupException(e);
        }
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
}
