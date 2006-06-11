package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.events.AsynchronousDelegatingListener;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.BuildCompletedEvent;
import com.zutubi.pulse.events.build.BuildRequestEvent;
import com.zutubi.pulse.events.build.BuildTerminationRequestEvent;
import com.zutubi.pulse.events.build.BuildTimeoutEvent;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseEvent;
import com.zutubi.pulse.license.LicenseExpiredEvent;
import com.zutubi.pulse.license.LicenseUpdateEvent;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.scheduling.quartz.TimeoutBuildJob;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.util.logging.Logger;
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
    private MasterConfigurationManager configManager;
    private RecipeQueue recipeQueue;

    private ReentrantLock lock = new ReentrantLock();
    private Condition stoppedCondition = lock.newCondition();
    private boolean stopping = false;
    private ProjectQueue projectQueue = new ProjectQueue();
    private Set<BuildController> runningBuilds = new HashSet<BuildController>();
    private Scheduler quartzScheduler;
    private ProjectManager projectManager;
    private ServiceTokenManager serviceTokenManager;

    /**
     * When the fat controller is enabled, it will handle incoming build requests.
     * If not, it will ignore them.
     */
    private boolean enabled = false;

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

        // check license: enable the fat controller iff the license is valid.
        License license = configManager.getData().getLicense();
        if (license == null || license.isExpired())
        {
            disable();
        }
        else
        {
            enable();
        }
    }

    /**
     * If enabled, this fat controller is accepting build requests.
     *
     * @see #isDisabled()
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * If disabled, this fat controller is ignoring build requests.
     *
     * @see #isEnabled()
     */
    public boolean isDisabled()
    {
        return !isEnabled();
    }

    /**
     * Sets the enabled state of this instance to true.
     *
     * @see #isEnabled()
     * @see #isDisabled()
     */
    private void enable()
    {
        enabled = true;
    }

    /**
     * Sets the enabled state of this instance to false.
     *
     * @see #isEnabled()
     * @see #isDisabled()
     */
    private void disable()
    {
        enabled = false;
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

    /**
     * Handle the incoming event.
     *
     * @param event
     */
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
        else if (event instanceof LicenseEvent)
        {
            handleLicenseEvent((LicenseEvent)event);
        }
    }

    private void handleLicenseEvent(LicenseEvent event)
    {
        if (event instanceof LicenseExpiredEvent)
        {
            disable();
        }
        else if (event instanceof LicenseUpdateEvent)
        {
            if (event.getLicense().isExpired())
            {
                disable();
            }
            else
            {
                enable();
            }
        }
    }


    private void handleBuildRequest(BuildRequestEvent event)
    {
        // we are disabled, so we ignore incoming build requests.
        if (isDisabled())
        {
            return;
        }

        if (event.getProject().isPaused())
        {
            // Ignore build requests while project is paused
            return;
        }

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
            // the build spec was deleted / renamed between the project build request being created and
            // the request triggering a start build.
            LOG.warning("Request to build unknown specification '" + specName + "' for project '" + project.getName() + "'");
            projectQueue.buildCompleted(project);
            return;
        }

        lock.lock();
        try
        {
            if (!stopping)
            {
                projectManager.buildCommenced(project.getId());
                RecipeResultCollector collector = new DefaultRecipeResultCollector(project, configManager);
                BuildController controller = new BuildController(event, buildSpec, eventManager, buildManager, recipeQueue, collector, quartzScheduler, configManager, serviceTokenManager);
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
        terminateBuild(event.getBuildId(), true);
    }

    public void terminateBuild(long id, boolean timeout)
    {
        lock.lock();
        try
        {
            for (BuildController controller : runningBuilds)
            {
                if (controller.getBuildId() == id)
                {
                    controller.handleEvent(new BuildTerminationRequestEvent(this, timeout));
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
            // Look up the project to avoid stale data
            Project project = projectManager.getProject(event.getResult().getProject().getId());

            BuildController controller = (BuildController) event.getSource();
            runningBuilds.remove(controller);

            if (runningBuilds.size() == 0)
            {
                stoppedCondition.signalAll();
            }

            projectManager.buildCompleted(project.getId());

            if (!stopping)
            {
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
        return new Class[]{BuildRequestEvent.class,
                BuildCompletedEvent.class,
                BuildTimeoutEvent.class,
                LicenseExpiredEvent.class,
                LicenseUpdateEvent.class
        };
    }

    /**
     * Retrieve a snapshot of the current project queue.
     *
     * @return a mapping of projects to queued build request events. These events will be
     * the order that these events will be handled.
     */
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

    /**
     * Required resource.
     *
     * @param buildManager
     */
    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    /**
     * Required resource.
     *
     * @param recipeQueue
     */
    public void setRecipeQueue(RecipeQueue recipeQueue)
    {
        this.recipeQueue = recipeQueue;
    }

    /**
     * Required resource.
     *
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    /**
     * Required resource.
     *
     * @param quartzScheduler
     */
    public void setQuartzScheduler(Scheduler quartzScheduler)
    {
        this.quartzScheduler = quartzScheduler;
    }

    /**
     * Required resource.
     *
     * @param configManager
     */
    public void setConfigurationManager(MasterConfigurationManager configManager)
    {
        this.configManager = configManager;
    }

    /**
     * Required resource.
     *
     * @param projectManager
     */
    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }
}
