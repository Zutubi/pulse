package com.zutubi.pulse.master.build.control;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.master.events.build.RecipeAssignedEvent;
import com.zutubi.pulse.master.events.build.RecipeDispatchedEvent;
import com.zutubi.util.logging.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
 * A simple service to farm assigned recipes out to the agents.  This is done
 * in a service as it requires a (possibly-slow) network call to the agent.
 */
public class RecipeDispatchService implements Runnable, Stoppable
{
    private static final Logger LOG = Logger.getLogger(RecipeDispatchService.class);

    private BlockingQueue<RecipeAssignedEvent> assignments = new LinkedBlockingQueue<RecipeAssignedEvent>();
    private ThreadFactory threadFactory;
    private EventManager eventManager;
    private boolean stopRequested = false;
    private Thread thread;

    public void init()
    {
        thread = threadFactory.newThread(this);
        thread.setDaemon(true);
        thread.setName("Recipe Dispatch Service");
        thread.start();
    }

    public void run()
    {
        while (!stopRequested)
        {
            RecipeAssignedEvent assignment;
            try
            {
                assignment = assignments.take();
            }
            catch (InterruptedException e)
            {
                continue;
            }

            try
            {
                assignment.getAgent().getService().build(assignment.getRequest());
                eventManager.publish(new RecipeDispatchedEvent(this, assignment.getRecipeId(), assignment.getAgent()));
            }
            catch (Exception e)
            {
                LOG.warning("Unable to dispatch recipe: " + e.getMessage(), e);
                eventManager.publish(new RecipeErrorEvent(this, assignment.getRecipeId(), "Unable to dispatch recipe: " + e.getMessage()));
            }
        }
    }

    public void dispatch(RecipeAssignedEvent assignedEvent)
    {
        assignments.offer(assignedEvent);
    }

    public void stop(boolean force)
    {
        this.stopRequested = true;
        thread.interrupt();
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
