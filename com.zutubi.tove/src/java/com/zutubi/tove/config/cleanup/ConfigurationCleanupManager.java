/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.tove.config.cleanup;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.tove.ConventionSupport;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.events.ConfigurationEvent;
import com.zutubi.tove.events.ConfigurationEventSystemStartedEvent;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.*;

/**
 */
public class ConfigurationCleanupManager implements EventListener
{
    private static final Logger LOG = Logger.getLogger(ConfigurationCleanupManager.class);

    private Map<Class, ConfigurationCleanupTaskFinder> findersByType = new HashMap<Class, ConfigurationCleanupTaskFinder>();
    private ConfigurationProvider configurationProvider;
    private ConfigurationTemplateManager configurationTemplateManager;
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

    public List<ConfigurationEvent> runCleanupTasks(RecordCleanupTask task, RecordManager recordManager)
    {
        List<ConfigurationEvent> events = new LinkedList<ConfigurationEvent>();
        runCleanupTasks(task, recordManager, events);
        return events;
    }

    private void runCleanupTasks(RecordCleanupTask task, RecordManager recordManager, List<ConfigurationEvent> events)
    {
        // We need to prepare events in advance as if, e.g. the task deletes
        // the path they cannot be determined afterwards.
        List<ConfigurationEvent> possibleEvents = null;
        switch (task.getCleanupAction())
        {
            case NONE:
                possibleEvents = Collections.emptyList();
                break;
            case DELETE:
                possibleEvents = configurationTemplateManager.prepareDirectDeleteEvents(task.getAffectedPath());
                break;
            case PARENT_UPDATE:
                possibleEvents = configurationTemplateManager.prepareDirectAndInheritedSaveEvents(PathUtils.getParentPath(task.getAffectedPath()));
                break;
        }
        
        if (task.run(recordManager))
        {
            for (ConfigurationEvent event: possibleEvents)
            {
                if (!events.contains(event))
                {
                    events.add(event);
                }
            }
        }

        for (RecordCleanupTask subTask : task.getCascaded())
        {
            runCleanupTasks(subTask, recordManager, events);
        }
    }

    public synchronized ConfigurationCleanupTaskFinder getCleanupTaskFinder(Class configurationClass)
    {
        ConfigurationCleanupTaskFinder finder = findersByType.get(configurationClass);
        if (finder == null)
        {
            finder = new ConfigurationCleanupTaskFinder(configurationClass, ConventionSupport.loadClass(configurationClass, "CleanupTasks", Object.class), objectFactory);
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

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
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
