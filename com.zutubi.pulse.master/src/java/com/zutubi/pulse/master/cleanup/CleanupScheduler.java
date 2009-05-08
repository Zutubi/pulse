package com.zutubi.pulse.master.cleanup;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.master.cleanup.requests.ProjectCleanupRequest;
import com.zutubi.pulse.master.cleanup.requests.UserCleanupRequest;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.scheduling.SchedulingException;
import com.zutubi.pulse.master.scheduling.SimpleTrigger;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.util.Constants;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

/**
 * The cleanup scheduler is responsible for the 'WHEN' of the cleanup processing within
 * Pulse, triggering and generating cleanup tasks that are sent to the cleanup manager
 * for execution.
 * <p/>
 * The cleanup is triggered in response to two inputs.
 * <ul>
 * <li>every time a build for that project completes</li>
 * <li>at regularly scheduled intervals</li>
 * </ul>
 */
public class CleanupScheduler implements Stoppable
{
    private static final Logger LOG = Logger.getLogger(CleanupScheduler.class);

    private static final String TRIGGER_NAME = "cleanup";
    private static final String TRIGGER_GROUP = "services";

    private EventManager eventManager;
    private EventListener eventListener;
    private Scheduler scheduler;
    private ObjectFactory objectFactory;
    private CleanupManager cleanupManager;

    public void init()
    {
        initEventScheduling();
        initPeriodicScheduling();
    }

    protected void initPeriodicScheduling()
    {
        Trigger trigger = scheduler.getTrigger(TRIGGER_NAME, TRIGGER_GROUP);
        if (trigger == null)
        {
            // initialise the trigger.
            trigger = new SimpleTrigger(TRIGGER_NAME, TRIGGER_GROUP, Constants.DAY);
            trigger.setTaskClass(CleanupBuilds.class);

            try
            {
                scheduler.schedule(trigger);
            }
            catch (SchedulingException e)
            {
                LOG.severe(e);
            }
        }
    }

    protected void initEventScheduling()
    {
        eventListener = new CleanupCallback();
        eventManager.register(eventListener);
    }

    public void stop(boolean force)
    {
        eventManager.unregister(eventListener);
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void setCleanupManager(CleanupManager cleanupManager)
    {
        this.cleanupManager = cleanupManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
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

        private ProjectCleanupRequest createRequest(Project project)
        {
            return objectFactory.buildBean(ProjectCleanupRequest.class, new Class[]{Project.class}, new Object[]{project});
        }

        private UserCleanupRequest createRequest(User user)
        {
            return objectFactory.buildBean(UserCleanupRequest.class, new Class[]{User.class}, new Object[]{user});
        }

        public Class[] getHandledEvents()
        {
            return new Class[]{BuildCompletedEvent.class};
        }
    }
}