package com.cinnamonbob;

import com.cinnamonbob.core.Stoppable;
import com.cinnamonbob.events.AsynchronousDelegatingListener;
import com.cinnamonbob.events.Event;
import com.cinnamonbob.events.EventListener;
import com.cinnamonbob.events.EventManager;
import com.cinnamonbob.events.build.BuildCompletedEvent;
import com.cinnamonbob.events.build.BuildRequestEvent;
import com.cinnamonbob.events.build.BuildTerminationRequestEvent;
import com.cinnamonbob.events.build.BuildTimeoutEvent;
import com.cinnamonbob.model.BuildManager;
import com.cinnamonbob.model.BuildSpecification;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.scheduling.quartz.TimeoutBuildJob;
import com.cinnamonbob.util.logging.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The FatController kicks off builds on receipt of BuildRequestEvents, and
 * manages multiple requests for the same project.  It is also a single point
 * for controlling all builds, for example to stop them during shutdown.
 */
public class FatController implements EventListener, Stoppable
{
    public static final String PARAM_EVENT_MANAGER = "eventManager";
    public static final String TIMEOUT_JOB_NAME = "build";
    public static final String TIMEOUT_JOB_GROUP = "timeout";

    private static final Logger LOG = Logger.getLogger(FatController.class);

    private EventManager eventManager;
    private AsynchronousDelegatingListener asyncListener;
    private BuildManager buildManager;
    private RecipeQueue recipeQueue;

    private ReentrantLock lock = new ReentrantLock();
    private Condition stoppedCondition = lock.newCondition();
    private boolean stopping = false;
    private ProjectQueue projectQueue = new ProjectQueue();
    private Set<BuildController> runningBuilds = new HashSet<BuildController>();
    private Scheduler quartzScheduler;

    public FatController()
    {
    }

    public void init() throws SchedulerException
    {
        asyncListener = new AsynchronousDelegatingListener(this);
        eventManager.register(asyncListener);

        JobDetail detail = new JobDetail(TIMEOUT_JOB_NAME, TIMEOUT_JOB_GROUP, TimeoutBuildJob.class);
        detail.getJobDataMap().put(PARAM_EVENT_MANAGER, eventManager);
        detail.setDurability(true); // will stay around after the trigger has gone.
        quartzScheduler.addJob(detail, true);
    }

    public void stop(boolean force)
    {
        // Make sure all controllers are done
        lock.lock();

        try
        {
            // Flag that no new builds should be initiated
            stopping = true;

            if (force)
            {
                // Notify controllers to stop forcefully
                eventManager.publish(new BuildTerminationRequestEvent(this, false));
            }
            else
            {
                // Wait for builds to complete
                LOG.info("Waiting for running builds to complete...");
                while (runningBuilds.size() > 0)
                {
                    try
                    {
                        stoppedCondition.await();
                    }
                    catch (InterruptedException e)
                    {
                        LOG.warning("Interrupted while waiting for running builds to complete", e);
                    }
                }
                LOG.info("All builds completed");
            }
        }
        finally
        {
            lock.unlock();
        }

        // Now stop handling events
        eventManager.unregister(asyncListener);
        asyncListener.stop(force);
    }

    public void handleEvent(Event event)
    {
        if (event instanceof BuildRequestEvent)
        {
            handleBuildRequest((BuildRequestEvent) event);
        }
        else if (event instanceof BuildCompletedEvent)
        {
            handleBuildCompleted((BuildCompletedEvent) event);
        }
        else if (event instanceof BuildTimeoutEvent)
        {
            handleBuildTimeout((BuildTimeoutEvent) event);
        }
    }

    private void handleBuildRequest(BuildRequestEvent event)
    {
        if (projectQueue.buildRequested(event))
        {
            startBuild(event);
        }
    }

    private void startBuild(BuildRequestEvent event)
    {
        final Project project = event.getProject();
        String specName = event.getSpecification();

        BuildSpecification buildSpec = project.getBuildSpecification(specName);
        if (buildSpec == null)
        {
            LOG.warning("Request to build unknown specification '" + specName + "' for project '" + project.getName() + "'");
            projectQueue.buildCompleted(project);
            return;
        }

        lock.lock();
        try
        {
            if (!stopping)
            {
                RecipeResultCollector collector = new DefaultRecipeResultCollector(project);
                BuildController controller = new BuildController(project, buildSpec, eventManager, buildManager, recipeQueue, collector, quartzScheduler);
                controller.run();
                runningBuilds.add(controller);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    private void handleBuildTimeout(BuildTimeoutEvent event)
    {
        // No worries if we don't find the controller: it may have finished
        long id = event.getBuildId();
        try
        {
            lock.lock();
            for (BuildController controller : runningBuilds)
            {
                if (controller.getBuildId() == id)
                {
                    controller.handleEvent(new BuildTerminationRequestEvent(this, true));
                    break;
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    private void handleBuildCompleted(BuildCompletedEvent event)
    {
        lock.lock();
        try
        {
            BuildController controller = (BuildController) event.getSource();
            runningBuilds.remove(controller);

            if (runningBuilds.size() == 0)
            {
                stoppedCondition.signalAll();
            }

            if (!stopping)
            {
                Project project = event.getResult().getProject();
                BuildRequestEvent queuedEvent = projectQueue.buildCompleted(project);

                if (queuedEvent != null)
                {
                    startBuild(queuedEvent);
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildRequestEvent.class, BuildCompletedEvent.class, BuildTimeoutEvent.class};
    }

    public Map<Project, List<BuildRequestEvent>> snapshotProjectQueue()
    {
        lock.lock();
        try
        {
            return projectQueue.takeSnapshot();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setRecipeQueue(RecipeQueue recipeQueue)
    {
        this.recipeQueue = recipeQueue;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setQuartzScheduler(Scheduler quartzScheduler)
    {
        this.quartzScheduler = quartzScheduler;
    }
}
