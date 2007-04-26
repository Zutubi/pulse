package com.zutubi.pulse.cleanup;

import com.zutubi.pulse.events.build.BuildCompletedEvent;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.scheduling.SimpleTrigger;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.util.Constants;
import com.zutubi.util.logging.Logger;

/**
 *
 *
 */
public class CleanupManager
{
    private static final Logger LOG = Logger.getLogger(CleanupManager.class);

    private final EventListener eventListener = new CleanupCallback();

    private EventManager eventManager;
    private Scheduler scheduler;
    private BuildManager buildManager;

    private static final String CLEANUP_NAME = "cleanup";
    private static final String CLEANUP_GROUP = "services";
    
    private static final long CLEANUP_FREQUENCY = Constants.HOUR;

    /**
     * Initialise the cleanup manager, registering event listeners and scheduling callbacks.
     */
    public void init()
    {
        // register project configuration.  This will eventually be handled as an extension point

        // register global configuration, global cleanup time? 


        // register for events.
        eventManager.register(eventListener);
        
        // register for scheduled callbacks.
        Trigger trigger = scheduler.getTrigger(CLEANUP_NAME, CLEANUP_GROUP);
        if (trigger != null)
        {
            return;
        }

        // initialise the trigger.
        trigger = new SimpleTrigger(CLEANUP_NAME, CLEANUP_GROUP, CLEANUP_FREQUENCY);
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

    public void cleanupBuilds()
    {
        buildManager.cleanupBuilds();
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    /**
     * Listen for build completed events, triggering each completed builds projects
     * cleanup routines.
     * 
     */
    private class CleanupCallback implements EventListener
    {
        public void handleEvent(Event evt)
        {
            BuildCompletedEvent completedEvent = (BuildCompletedEvent) evt;
            BuildResult result = completedEvent.getResult();
            if(result.isPersonal())
            {
                buildManager.cleanupBuilds(result.getUser());
            }
            else
            {
                buildManager.cleanupBuilds(result.getProject());
            }
        }

        public Class[] getHandledEvents()
        {
            return new Class[]{BuildCompletedEvent.class};
        }
    }
}
