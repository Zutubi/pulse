package com.zutubi.pulse;

import com.zutubi.prototype.security.AccessManager;
import com.zutubi.pulse.agent.AgentManager;
import com.zutubi.pulse.agent.MasterLocationProvider;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.scm.DelegateScmClientFactory;
import com.zutubi.pulse.events.AsynchronousDelegatingListener;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.build.AbstractBuildRequestEvent;
import com.zutubi.pulse.events.build.BuildCompletedEvent;
import com.zutubi.pulse.events.build.BuildTerminationRequestEvent;
import com.zutubi.pulse.events.build.RecipeTimeoutEvent;
import com.zutubi.pulse.events.system.SystemStartedEvent;
import com.zutubi.pulse.license.License;
import com.zutubi.pulse.license.LicenseHolder;
import com.zutubi.pulse.license.events.LicenseEvent;
import com.zutubi.pulse.license.events.LicenseExpiredEvent;
import com.zutubi.pulse.license.events.LicenseUpdateEvent;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.scheduling.quartz.TimeoutRecipeJob;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.util.logging.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
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

    private Lock runningBuildsLock = new ReentrantLock();
    private Condition stoppedCondition = runningBuildsLock.newCondition();
    private boolean stopping = false;
    private Set<BuildController> runningBuilds = new HashSet<BuildController>();
    private Lock buildQueueLock = new ReentrantLock();
    private BuildQueue buildQueue = new BuildQueue();
    private Scheduler quartzScheduler;
    private ProjectManager projectManager;
    private AgentManager agentManager;
    private UserManager userManager;
    private ServiceTokenManager serviceTokenManager;
    private ThreadFactory threadFactory;
    private AccessManager accessManager;
    private MasterLocationProvider masterLocationProvider;
    private ResourceManager resourceManager;

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
        buildQueue.setAccessManager(accessManager);
        asyncListener = new AsynchronousDelegatingListener(this, threadFactory);
        eventManager.register(asyncListener);

        JobDetail detail = new JobDetail(TIMEOUT_JOB_NAME, TIMEOUT_JOB_GROUP, TimeoutRecipeJob.class);
        detail.getJobDataMap().put(PARAM_EVENT_MANAGER, eventManager);
        detail.setDurability(true); // will stay around after the trigger has gone.
        quartzScheduler.addJob(detail, true);
    }

    private boolean licensedToBuild()
    {
        // First check we can run (handles eval expiry and illegal upgrades
        // for commercial license) and then ensure we are within our limits.
        License license = LicenseHolder.getLicense();
        return license.canRunVersion(Version.getVersion()) && !license.isExceeded(projectManager.getProjectCount(), agentManager.getAgentCount(), userManager.getUserCount());
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
        runningBuildsLock.lock();
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
            runningBuildsLock.unlock();
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
        else if (event instanceof SystemStartedEvent)
        {
            // check license: enable the fat controller iff the license is valid.
            if (licensedToBuild())
            {
                enable();
            }
            else
            {
                disable();
            }
        }
    }

    private void handleLicenseEvent()
    {
        // the type or detail of the license event does not matter at this stage.
        if (licensedToBuild())
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
            LOG.warning("Build request ignored as license is expired or exceeded.");
            return;
        }

        Project project = projectManager.getProject(event.getProjectConfig().getProjectId(), false);
        if (!event.isPersonal() && project.isPaused())
        {
            // Ignore build requests while project is paused
            return;
        }

        boolean requested;
        buildQueueLock.lock();
        try
        {
            requested = buildQueue.buildRequested(event);
        }
        finally
        {
            buildQueueLock.unlock();
        }

        if (requested)
        {
            startBuild(event);
        }
    }

    private void startBuild(AbstractBuildRequestEvent event)
    {
        final Project project = projectManager.getProject(event.getProjectConfig().getProjectId(), false);

        runningBuildsLock.lock();
        try
        {
            if (!stopping)
            {
                if(!event.isPersonal())
                {
                    projectManager.buildCommenced(project.getId());
                }

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
                controller.setMasterLocationProvider(masterLocationProvider);
                controller.setThreadFactory(threadFactory);
                controller.setResourceManager(resourceManager);
                controller.init();
                controller.run();
                runningBuilds.add(controller);
            }
        }
        finally
        {
            runningBuildsLock.unlock();
        }
    }

    public void terminateBuild(long id, boolean timeout)
    {
        eventManager.publish(new BuildTerminationRequestEvent(this, id, timeout));
    }

    private void handleBuildCompleted(BuildCompletedEvent event)
    {
        runningBuildsLock.lock();
        try
        {
            BuildResult result = event.getBuildResult();
            BuildController controller = (BuildController) event.getSource();
            runningBuilds.remove(controller);

            if (runningBuilds.size() == 0)
            {
                stoppedCondition.signalAll();
            }

            if(!result.isPersonal())
            {
                projectManager.buildCompleted(result.getProject().getId(), result.succeeded());
            }

            if (!stopping)
            {
                Entity owner = result.getOwner();
                AbstractBuildRequestEvent queuedEvent;
                buildQueueLock.lock();
                try
                {
                    queuedEvent = buildQueue.buildCompleted(owner);
                }
                finally
                {
                    buildQueueLock.unlock();
                }
                
                if (queuedEvent != null)
                {
                    startBuild(queuedEvent);
                }
            }
        }
        finally
        {
            runningBuildsLock.unlock();
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{AbstractBuildRequestEvent.class,
                BuildCompletedEvent.class,
                RecipeTimeoutEvent.class,
                LicenseExpiredEvent.class,
                LicenseUpdateEvent.class,
                SystemStartedEvent.class
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
        buildQueueLock.lock();
        try
        {
            return buildQueue.takeSnapshot();
        }
        finally
        {
            buildQueueLock.unlock();
        }
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
        buildQueueLock.lock();
        try
        {
            return buildQueue.cancelBuild(id);
        }
        finally
        {
            buildQueueLock.unlock();
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

    public void setTestManager(TestManager testManager)
    {
        this.testManager = testManager;
    }

    public void setScmClientFactory(DelegateScmClientFactory scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setMasterLocationProvider(MasterLocationProvider masterLocationProvider)
    {
        this.masterLocationProvider = masterLocationProvider;
    }
}
