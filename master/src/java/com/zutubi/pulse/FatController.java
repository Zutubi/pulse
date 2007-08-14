package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.events.AsynchronousDelegatingListener;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.AbstractBuildRequestEvent;
import com.zutubi.pulse.events.build.BuildCompletedEvent;
import com.zutubi.pulse.events.build.BuildTerminationRequestEvent;
import com.zutubi.pulse.events.build.RecipeTimeoutEvent;
import com.zutubi.pulse.license.events.LicenseEvent;
import com.zutubi.pulse.license.events.LicenseExpiredEvent;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.license.events.LicenseUpdateEvent;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.scheduling.quartz.TimeoutRecipeJob;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.core.scm.DelegateScmClientFactory;
import com.zutubi.util.logging.Logger;
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
    private TestManager testManager;
    private DelegateScmClientFactory scmClientFactory;
    private MasterConfigurationManager configManager;
    private RecipeQueue recipeQueue;

    private ReentrantLock lock = new ReentrantLock();
    private Condition stoppedCondition = lock.newCondition();
    private boolean stopping = false;
    private BuildQueue buildQueue = new BuildQueue();
    private Set<BuildController> runningBuilds = new HashSet<BuildController>();
    private Scheduler quartzScheduler;
    private ProjectManager projectManager;
    private UserManager userManager;
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

        JobDetail detail = new JobDetail(TIMEOUT_JOB_NAME, TIMEOUT_JOB_GROUP, TimeoutRecipeJob.class);
        detail.getJobDataMap().put(PARAM_EVENT_MANAGER, eventManager);
        detail.setDurability(true); // will stay around after the trigger has gone.
        quartzScheduler.addJob(detail, true);

        // check license: enable the fat controller iff the license is valid.
        if (LicenseHolder.hasAuthorization(LicenseHolder.AUTH_RUN_PULSE))
        {
            enable();
        }
        else
        {
            disable();
        }
    }

    /**
     * @return true if this fat controller is accepting build requests.
     *
     * @see #isDisabled()
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * @return true if this fat controller is ignoring build requests.
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
                eventManager.publish(new BuildTerminationRequestEvent(this, -1, false));
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
        if (event instanceof AbstractBuildRequestEvent)
        {
            handleBuildRequest((AbstractBuildRequestEvent) event);
        }
        else if (event instanceof BuildCompletedEvent)
        {
            handleBuildCompleted((BuildCompletedEvent) event);
        }
        else if (event instanceof LicenseEvent)
        {
            handleLicenseEvent();
        }
    }

    private void handleLicenseEvent()
    {
        // the type or detail of the license event does not matter at this stage.
        if (LicenseHolder.hasAuthorization(LicenseHolder.AUTH_RUN_PULSE))
        {
            enable();
        }
        else
        {
            disable();
        }
    }

    private void handleBuildRequest(AbstractBuildRequestEvent event)
    {
        // if we are disabled, we ignore incoming build requests.
        if (isDisabled())
        {
            return;
        }

        Project project = projectManager.getProject(event.getProjectConfig().getProjectId());
        if (!event.isPersonal() && project.isPaused())
        {
            // Ignore build requests while project is paused
            return;
        }

        if (buildQueue.buildRequested(event))
        {
            startBuild(event);
        }
    }

    private void startBuild(AbstractBuildRequestEvent event)
    {
        final Project project = projectManager.getProject(event.getProjectConfig().getProjectId());

        lock.lock();
        try
        {
            if (!stopping)
            {
                if(!event.isPersonal())
                {
                    projectManager.buildCommenced(project.getId());
                }

                // FIXME: move the controller creation into the spring context so that we do not need to
                // do this manual wiring.
                DefaultRecipeResultCollector collector = new DefaultRecipeResultCollector(configManager);
                collector.setProjectConfig(event.getProjectConfig());
                BuildController controller = new BuildController(event);
                controller.setBuildManager(buildManager);
                controller.setCollector(collector);
                controller.setEventManager(eventManager);
                controller.setProjectManager(projectManager);
                controller.setQuartzScheduler(quartzScheduler);
                controller.setQueue(recipeQueue);
                controller.setServiceTokenManager(serviceTokenManager);
                controller.setTestManager(testManager);
                controller.setScmClientFactory(scmClientFactory);
                controller.setUserManager(userManager);
                controller.setConfigurationManager(configManager);
                controller.init();
                controller.run();
                runningBuilds.add(controller);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    public void terminateBuild(long id, boolean timeout)
    {
        eventManager.publish(new BuildTerminationRequestEvent(this, id, timeout));
    }

    private void handleBuildCompleted(BuildCompletedEvent event)
    {
        lock.lock();
        try
        {
            BuildResult result = event.getResult();

            // Look up the project to avoid stale data
            Project project = projectManager.getProject(result.getProject().getId());

            BuildController controller = (BuildController) event.getSource();
            runningBuilds.remove(controller);

            if (runningBuilds.size() == 0)
            {
                stoppedCondition.signalAll();
            }

            if(!result.isPersonal())
            {
                projectManager.buildCompleted(project.getId());
            }

            if (!stopping)
            {
                Entity owner = result.getOwner();
                AbstractBuildRequestEvent queuedEvent = buildQueue.buildCompleted(owner);

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
        return new Class[]{AbstractBuildRequestEvent.class,
                BuildCompletedEvent.class,
                RecipeTimeoutEvent.class,
                LicenseExpiredEvent.class,
                LicenseUpdateEvent.class
        };
    }

    /**
     * Retrieve a snapshot of the current build queue.
     *
     * @return a mapping of owners to queued build request events. These events will be
     * the order that these events will be handled.
     */
    public Map<Object, List<AbstractBuildRequestEvent>> snapshotBuildQueue()
    {
        lock.lock();
        try
        {
            return buildQueue.takeSnapshot();
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

    public void setConfigurationManager(MasterConfigurationManager configManager)
    {
        this.configManager = configManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    /**
     * Cancel the queued build.
     *
     * @param id uniquely representing the queued build.
     *
     * @return true if the build has been cancelled, false otherwise.
     */
    public boolean cancelQueuedBuild(long id)
    {
        return buildQueue.cancelBuild(id);
    }

    public void setTestManager(TestManager testManager)
    {
        this.testManager = testManager;
    }

    public void setScmClientFactory(DelegateScmClientFactory scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }
}
