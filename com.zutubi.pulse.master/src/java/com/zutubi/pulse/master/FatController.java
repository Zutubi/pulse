package com.zutubi.pulse.master;

import com.zutubi.events.AsynchronousDelegatingListener;
import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.events.build.AbstractBuildRequestEvent;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.events.build.BuildTerminationRequestEvent;
import com.zutubi.pulse.master.license.License;
import com.zutubi.pulse.master.license.LicenseHolder;
import com.zutubi.pulse.master.license.events.LicenseEvent;
import com.zutubi.pulse.master.license.events.LicenseExpiredEvent;
import com.zutubi.pulse.master.license.events.LicenseUpdateEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.scheduling.quartz.TimeoutRecipeJob;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.pulse.servercore.events.system.SystemStartedEvent;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

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
    public static final String TIMEOUT_JOB_NAME = "build";
    public static final String TIMEOUT_JOB_GROUP = "timeout";

    private static final Logger LOG = Logger.getLogger(FatController.class);

    private EventManager eventManager;
    private AsynchronousDelegatingListener asyncListener;

    private Lock lock = new ReentrantLock();
    private Condition stoppedCondition = lock.newCondition();
    private BuildQueue buildQueue = new BuildQueue();
    private Scheduler quartzScheduler;
    private ProjectManager projectManager;
    private AgentManager agentManager;
    private UserManager userManager;
    private ThreadFactory threadFactory;
    private AccessManager accessManager;

    /**
     * When the fat controller is enabled, it will handle incoming build requests.
     * If not, it will ignore them.
     */
    private boolean enabled = false;
    private ObjectFactory objectFactory;

    public FatController()
    {
    }

    public void init() throws SchedulerException
    {
        buildQueue.setObjectFactory(objectFactory);
        asyncListener = new AsynchronousDelegatingListener(this, threadFactory);
        eventManager.register(asyncListener);

        JobDetail detail = new JobDetail(TIMEOUT_JOB_NAME, TIMEOUT_JOB_GROUP, TimeoutRecipeJob.class);
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
        lock.lock();
        try
        {
            // Notify the queue to not activate any more builds.
            buildQueue.stop();

            if (force)
            {
                // Notify controllers to stop forcefully
                eventManager.publish(new BuildTerminationRequestEvent(this, -1, "due to server shutdown"));
            }
            else
            {
                // Wait for builds to complete
                LOG.info("Waiting for running builds to complete...");
                while (buildQueue.getActiveBuildCount() > 0)
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

        long projectId = event.getProjectConfig().getProjectId();
        projectManager.lockProjectStates(projectId);
        try
        {
            Project project = projectManager.getProject(projectId, false);
            if (!project.getState().acceptTrigger(event.isPersonal()))
            {
                // Ignore build requests while project is not able to be built
                // (e.g. if it is pausing).
                return;
            }

            lock.lock();
            boolean newlyActive = !event.isPersonal() && buildQueue.getActiveBuildCount(project) == 0;
            try
            {
                buildQueue.buildRequested(event);
            }
            finally
            {
                lock.unlock();
            }

            if (newlyActive)
            {
                projectManager.makeStateTransition(projectId, Project.Transition.BUILDING);
            }
        }
        finally
        {
            projectManager.unlockProjectStates(projectId);
        }
    }

    public void terminateBuild(BuildResult buildResult, String reason)
    {
        accessManager.ensurePermission(ProjectConfigurationActions.ACTION_CANCEL_BUILD, buildResult);
        eventManager.publish(new BuildTerminationRequestEvent(this, buildResult.getId(), reason));
    }

    private void handleBuildCompleted(BuildCompletedEvent event)
    {
        BuildResult buildResult = event.getBuildResult();
        long projectId = buildResult.getProject().getId();

        if (!buildResult.isPersonal())
        {
            projectManager.lockProjectStates(projectId);
        }

        boolean newlyIdle = false;
        try
        {
            lock.lock();
            try
            {
                buildQueue.buildCompleted(event.getBuildResult().getOwner(), event.getBuildResult().getId());
                newlyIdle = !buildResult.isPersonal() && buildQueue.getActiveBuildCount(buildResult.getProject()) == 0;

                if (buildQueue.getActiveBuildCount() == 0)
                {
                    stoppedCondition.signalAll();
                }
            }
            finally
            {
                lock.unlock();
            }
        }
        finally
        {
            if (newlyIdle)
            {
                projectManager.makeStateTransition(projectId, Project.Transition.IDLE);
            }

            if (!buildResult.isPersonal())
            {
                projectManager.unlockProjectStates(projectId);
            }
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{AbstractBuildRequestEvent.class,
                BuildCompletedEvent.class,
                LicenseExpiredEvent.class,
                LicenseUpdateEvent.class,
                SystemStartedEvent.class
        };
    }

    /**
     * Retrieve a snapshot of the current build queue.
     *
     * @return a consistent view across the build queue at a single moment in
     *         time.
     */
    public BuildQueue.Snapshot snapshotBuildQueue()
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

    /**
     * Cancel the queued build.
     *
     * @param id uniquely representing the queued build.
     *
     * @return true if the build has been cancelled, false otherwise.
     */
    public boolean cancelQueuedBuild(long id)
    {
        lock.lock();
        try
        {
            return buildQueue.cancelBuild(id);
        }
        finally
        {
            lock.unlock();
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setQuartzScheduler(Scheduler quartzScheduler)
    {
        this.quartzScheduler = quartzScheduler;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    public void setAgentManager(AgentManager agentManager)
    {
        this.agentManager = agentManager;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }
}
