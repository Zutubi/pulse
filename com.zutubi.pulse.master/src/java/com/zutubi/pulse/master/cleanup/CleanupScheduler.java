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

package com.zutubi.pulse.master.cleanup;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.master.cleanup.config.AbstractCleanupConfiguration;
import com.zutubi.pulse.master.cleanup.requests.ProjectCleanupRequest;
import com.zutubi.pulse.master.cleanup.requests.UserCleanupRequest;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.scheduling.CallbackService;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.config.TypeAdapter;
import com.zutubi.tove.events.ConfigurationEventSystemStartedEvent;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.Constants;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.LinkedList;
import java.util.List;

/**
 * The cleanup scheduler is responsible for the 'WHEN' of the cleanup processing within
 * Pulse, triggering and generating cleanup tasks that are sent to the cleanup manager
 * for execution.
 * <p/>
 * The cleanup is triggered in response to three inputs:
 * <ul>
 * <li>every time a build for a project completes</li>
 * <li>every time the rules for a project changes</li>
 * <li>at regularly scheduled intervals</li>
 * </ul>
 */
public class CleanupScheduler implements Stoppable
{
    private static final Logger LOG = Logger.getLogger(CleanupScheduler.class);
    private static final String CALLBACK_NAME = "Cleanup";
    private EventManager eventManager;
    private EventListener eventListener;
    private CallbackService callbackService;
    private ObjectFactory objectFactory;
    private CleanupManager cleanupManager;
    private ProjectManager projectManager;

    public void init()
    {
        initEventScheduling();
        initPeriodicScheduling();
    }

    protected void initEventScheduling()
    {
        eventListener = new CleanupCallback();
        eventManager.register(eventListener);
        eventManager.register(new EventListener()
        {
            public void handleEvent(Event event)
            {
                initConfigScheduling(((ConfigurationEventSystemStartedEvent) event).getConfigurationProvider());
            }

            public Class[] getHandledEvents()
            {
                return new Class[]{ ConfigurationEventSystemStartedEvent.class };
            }
        });
    }

    private void initConfigScheduling(final ConfigurationProvider configurationProvider)
    {
        TypeAdapter<AbstractCleanupConfiguration> listener = new TypeAdapter<AbstractCleanupConfiguration>(AbstractCleanupConfiguration.class){
            private void scheduleProjectCleanup(AbstractCleanupConfiguration instance)
            {
                String projectPath = PathUtils.getPrefix(instance.getConfigurationPath(), 2);
                ProjectConfiguration projectConfig = configurationProvider.get(projectPath, ProjectConfiguration.class);
                if (projectConfig != null)
                {
                    Project project = projectManager.getProject(projectConfig.getProjectId(), false);
                    if (project != null)
                    {
                        cleanupManager.process(createRequest(project));
                    }
                }
            }

            @Override
            public void postInsert(AbstractCleanupConfiguration instance)
            {
                scheduleProjectCleanup(instance);
            }

            @Override
            public void postDelete(AbstractCleanupConfiguration instance)
            {
                scheduleProjectCleanup(instance);
            }

            @Override
            public void postSave(AbstractCleanupConfiguration instance, boolean nested)
            {
                scheduleProjectCleanup(instance);
            }
        };

        configurationProvider.registerEventListener(listener, false, false, PathUtils.getPath(MasterConfigurationRegistry.PROJECTS_SCOPE, PathUtils.WILDCARD_ANY_ELEMENT, MasterConfigurationRegistry.EXTENSION_PROJECT_CLEANUP, PathUtils.WILDCARD_ANY_ELEMENT));
    }

    protected void initPeriodicScheduling()
    {
        try
        {
            callbackService.registerCallback(CALLBACK_NAME, new Runnable()
            {
                public void run()
                {
                    scheduleProjectCleanup();
                }
            }, Constants.DAY);
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }
    }

    public void scheduleProjectCleanup()
    {
        List<Runnable> requests = new LinkedList<Runnable>();
        List<Project> projects = projectManager.getProjects(false);
        for (Project project : projects)
        {
            requests.add(createRequest(project));
        }
        cleanupManager.process(requests);
    }

    private ProjectCleanupRequest createRequest(Project project)
    {
        return objectFactory.buildBean(ProjectCleanupRequest.class, project);
    }

    private UserCleanupRequest createRequest(User user)
    {
        return objectFactory.buildBean(UserCleanupRequest.class, user);
    }

    public void stop(boolean force)
    {
        eventManager.unregister(eventListener);
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setCallbackService(CallbackService callbackService)
    {
        this.callbackService = callbackService;
    }

    public void setCleanupManager(CleanupManager cleanupManager)
    {
        this.cleanupManager = cleanupManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    /**
     * Listen for build completed events, triggering each completed builds projects
     * cleanup routines.
     */
    private class CleanupCallback implements EventListener
    {
        public void handleEvent(Event evt)
        {
            BuildCompletedEvent completedEvent = (BuildCompletedEvent) evt;
            BuildResult result = completedEvent.getBuildResult();

            if (result.isPersonal())
            {
                cleanupManager.process(createRequest(result.getUser()));
            }
            else
            {
                cleanupManager.process(createRequest(result.getProject()));
            }
        }

        public Class[] getHandledEvents()
        {
            return new Class[]{BuildCompletedEvent.class};
        }
    }
}