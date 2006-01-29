package com.cinnamonbob;

import com.cinnamonbob.core.Stoppable;
import com.cinnamonbob.core.event.AsynchronousDelegatingListener;
import com.cinnamonbob.core.event.Event;
import com.cinnamonbob.core.event.EventListener;
import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.events.build.BuildCompletedEvent;
import com.cinnamonbob.events.build.BuildRequestEvent;
import com.cinnamonbob.events.build.BuildTerminationRequestEvent;
import com.cinnamonbob.model.BuildManager;
import com.cinnamonbob.model.BuildSpecification;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.util.logging.Logger;

import java.util.HashMap;
import java.util.HashSet;
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
    private static final Logger LOG = Logger.getLogger(FatController.class);

    private EventManager eventManager;
    private AsynchronousDelegatingListener asyncListener;
    private BuildManager buildManager;
    private RecipeQueue recipeQueue;

    private ReentrantLock lock = new ReentrantLock();
    private Condition stoppedCondition = lock.newCondition();
    private boolean stopping = false;
    private Map<Project, BuildRequestEvent> activeProjects = new HashMap<Project, BuildRequestEvent>();
    private Set<BuildController> runningBuilds = new HashSet<BuildController>();

    public FatController()
    {
    }

    public void init()
    {
        asyncListener = new AsynchronousDelegatingListener(this);
        eventManager.register(asyncListener);
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
                eventManager.publish(new BuildTerminationRequestEvent(this));
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
    }

    private void handleBuildRequest(BuildRequestEvent event)
    {
        final Project project = event.getProject();
        String specName = event.getSpecification();

        if (activeProjects.containsKey(project))
        {
            activeProjects.put(project, event);
        }
        else
        {
            activeProjects.put(project, null);

            BuildSpecification buildSpec = project.getBuildSpecification(specName);
            if (buildSpec == null)
            {
                LOG.warning("Request to build unknown specification '" + specName + "' for project '" + project.getName() + "'");
                return;
            }

            lock.lock();
            try
            {
                if (!stopping)
                {
                    RecipeResultCollector collector = new DefaultRecipeResultCollector(project);
                    BuildController controller = new BuildController(project, buildSpec, eventManager, buildManager, recipeQueue, collector);
                    controller.run();
                    runningBuilds.add(controller);
                }
            }
            finally
            {
                lock.unlock();
            }
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
                BuildRequestEvent queuedEvent = activeProjects.remove(project);

                if (queuedEvent != null)
                {
                    handleBuildRequest(queuedEvent);
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
        return new Class[]{BuildRequestEvent.class, BuildCompletedEvent.class};
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
}
