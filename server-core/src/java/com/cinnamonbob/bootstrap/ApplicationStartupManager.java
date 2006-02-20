package com.cinnamonbob.bootstrap;

import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.events.EventManager;
import com.cinnamonbob.events.system.SystemStartedEvent;

import java.util.List;
import java.lang.reflect.Constructor;

/**
 *
 */
public class ApplicationStartupManager extends AbstractSystemStartupManager
{
    private static final Logger LOG = Logger.getLogger(ApplicationStartupManager.class);

    private List<String> startupRunnables;
    private List<String> subSystemContexts;

    public void setStartupRunnables(List<String> startupRunnables)
    {
        this.startupRunnables = startupRunnables;
    }

    public void setSubSystemContexts(List<String> subSystemContexts)
    {
        this.subSystemContexts = subSystemContexts;
    }

    protected void runStartup() throws StartupException
    {
        // load the subSystemContexts
        for (String subContext : subSystemContexts)
        {
            ComponentContext.addClassPathContextDefinitions(new String[]{subContext});
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

        EventManager eventManager = (EventManager) ComponentContext.getBean("eventManager");
        eventManager.publish(new SystemStartedEvent(this));
    }
}