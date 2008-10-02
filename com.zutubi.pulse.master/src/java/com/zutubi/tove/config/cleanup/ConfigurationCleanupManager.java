package com.zutubi.tove.config.cleanup;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.master.events.system.ConfigurationEventSystemStartedEvent;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class ConfigurationCleanupManager implements EventListener
{
    private static final Logger LOG = Logger.getLogger(ConfigurationCleanupManager.class);

    private Map<Class, ConfigurationCleanupTaskFinder> findersByType = new HashMap<Class, ConfigurationCleanupTaskFinder>();
    private ConfigurationProvider configurationProvider;
    private ObjectFactory objectFactory;

    public void addCustomCleanupTasks(RecordCleanupTaskSupport topTask)
    {
        String path = topTask.getAffectedPath();
        Configuration instance = configurationProvider.get(path, Configuration.class);

        if (instance != null)
        {
            Class<? extends Configuration> clazz = instance.getClass();
            try
            {
                List<RecordCleanupTask> customTasks = getCleanupTaskFinder(clazz).getCleanupTasks(instance);
                for (RecordCleanupTask task : customTasks)
                {
                    topTask.addCascaded(task);
                }
            }
            catch (Exception e)
            {
                LOG.severe("Unable to look up custom cleanup tasks for class '" + clazz.getName() + "': " + e.getMessage(), e);
            }
        }
    }

    public void runCleanupTasks(RecordCleanupTask task)
    {
        AcegiUtils.runAsSystem(task);

        for (RecordCleanupTask subTask : task.getCascaded())
        {
            runCleanupTasks(subTask);
        }
    }

    public synchronized ConfigurationCleanupTaskFinder getCleanupTaskFinder(Class configurationClass)
    {
        ConfigurationCleanupTaskFinder finder = findersByType.get(configurationClass);
        if (finder == null)
        {
            finder = new ConfigurationCleanupTaskFinder(configurationClass, ConventionSupport.getCleanupTasks(configurationClass), objectFactory);
            findersByType.put(configurationClass, finder);
        }

        return finder;
    }

    public void handleEvent(Event event)
    {
        configurationProvider = ((ConfigurationEventSystemStartedEvent)event).getConfigurationProvider();
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{ ConfigurationEventSystemStartedEvent.class };
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }
}
