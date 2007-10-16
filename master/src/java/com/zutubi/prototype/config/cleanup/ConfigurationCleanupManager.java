package com.zutubi.prototype.config.cleanup;

import com.zutubi.prototype.ConventionSupport;
import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.security.PulseThreadFactory;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 */
public class ConfigurationCleanupManager
{
    private static final Logger LOG = Logger.getLogger(ConfigurationCleanupManager.class);

    private Map<Class, ConfigurationCleanupTaskFinder> findersByType = new HashMap<Class, ConfigurationCleanupTaskFinder>();
    private Executor executor;
    private ConfigurationTemplateManager configurationTemplateManager;
    private ObjectFactory objectFactory;
    private PulseThreadFactory threadFactory;

    public void init()
    {
        executor = Executors.newCachedThreadPool(threadFactory);
    }

    public void addCustomCleanupTasks(RecordCleanupTaskSupport topTask)
    {
        String path = topTask.getAffectedPath();
        Configuration instance = configurationTemplateManager.getInstance(path);

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
        try
        {
            if (task.isAsynchronous())
            {
                executor.execute(task);
            }
            else
            {
                AcegiUtils.runAsSystem(task);
            }
        }
        catch (Exception e)
        {
            LOG.severe("Unable to run cleanup task on path '" + task.getAffectedPath() + "': " + e.getMessage(), e);
        }

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

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setThreadFactory(PulseThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }
}
