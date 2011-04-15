package com.zutubi.pulse.master.build.queue;

import com.zutubi.events.AsynchronousDelegatingListener;
import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.core.Stoppable;
import com.zutubi.pulse.core.model.NamedEntity;
import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.events.build.BuildCompletedEvent;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.events.build.BuildTerminationRequestEvent;
import com.zutubi.pulse.master.license.License;
import com.zutubi.pulse.master.license.LicenseHolder;
import com.zutubi.pulse.master.license.events.LicenseExpiredEvent;
import com.zutubi.pulse.master.license.events.LicenseUpdateEvent;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.servercore.events.system.SystemStartedEvent;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The fat controller is responsible for managing the scheduling system.
 *
 * This includes its initialisation, startup and shutdown, monitoring the
 * license etc etc.  Essentially, the scheduling systems integration with
 * the rest of pulse.
 */
public class FatController implements EventListener, Stoppable
{
    private static final Logger LOG = Logger.getLogger(FatController.class);
    private static final Messages I18N = Messages.getInstance(FatController.class);

    private EventManager eventManager;
    private AsynchronousDelegatingListener asyncListener;
    private ControllerStateListener controllerStateListener;

    private final Lock lock = new ReentrantLock();
    private Condition stoppedCondition = lock.newCondition();
    private ThreadFactory threadFactory;
    private BuildRequestRegistry buildRequestRegistry;

    /**
     * When the fat controller is enabled, it will handle incoming build requests.
     * If not, it will ignore them.
     */
    private boolean enabled = false;
    private ObjectFactory objectFactory;

    private SchedulingController schedulingController;

    public FatController()
    {
    }

    public void init()
    {
        asyncListener = new AsynchronousDelegatingListener(this, threadFactory);
        eventManager.register(asyncListener);

        controllerStateListener = objectFactory.buildBean(ControllerStateListener.class);
        controllerStateListener.setController(this);
        eventManager.register(controllerStateListener);
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
    void enable()
    {
        enabled = true;
    }

    /**
     * Sets the enabled state of this instance to false.
     *
     * @see #isEnabled()
     * @see #isDisabled()
     */
    void disable()
    {
        enabled = false;
    }

    public void stop(boolean force)
    {
        lock.lock();
        try
        {
            // Notify the queue to not activate any more builds.
            schedulingController.stop();

            if (force)
            {
                // Notify controllers to stop forcefully
                eventManager.publish(new BuildTerminationRequestEvent(this, "due to server shutdown"));
            }
            else
            {
                // Wait for builds to complete
                LOG.info("Waiting for running builds to complete...");
                while (schedulingController.getActivedRequestCount() > 0)
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
        eventManager.unregister(controllerStateListener);
        asyncListener.stop(force);
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{BuildRequestEvent.class, BuildCompletedEvent.class};
    }

    public void handleEvent(Event event)
    {
        if (event instanceof BuildRequestEvent)
        {
            requestBuild((BuildRequestEvent) event);
        }
        else if (event instanceof BuildCompletedEvent)
        {
            handleBuildCompleted((BuildCompletedEvent) event);
        }
    }

    public void requestBuild(BuildRequestEvent request)
    {
        if (isDisabled())
        {
            buildRequestRegistry.requestRejected(request, I18N.format("rejected.license.exceeded"));
            LOG.warning("Build request ignored as license is expired or exceeded.");
            return;
        }

        lock.lock();
        try
        {
            schedulingController.handleEvent(request);
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
            schedulingController.handleEvent(event);
            if (schedulingController.getActivedRequestCount() == 0)
            {
                stoppedCondition.signalAll();
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Retrieve a snapshot of the current build queue.
     *
     * @return a consistent view across the build queue at a single moment in
     *         time.
     */
    public BuildQueueSnapshot snapshotBuildQueue()
    {
        lock.lock();
        try
        {
            return schedulingController.getSnapshot();
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Returns a list of all queued and active build requests for a given
     * entity.  The latest request is first in the queue.
     *
     * @param entity the entity to get requests for
     * @return all queued and active requests for the given entity
     */
    public List<BuildRequestEvent> getRequestsForEntity(NamedEntity entity)
    {
        lock.lock();
        try
        {
            return schedulingController.getRequestsByOwner(entity);
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
            return schedulingController.cancelRequest(id);
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

    public void setThreadFactory(ThreadFactory threadFactory)
    {
        this.threadFactory = threadFactory;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setBuildRequestRegistry(BuildRequestRegistry buildRequestRegistry)
    {
        this.buildRequestRegistry = buildRequestRegistry;
    }

    public void setSchedulingController(SchedulingController schedulingController)
    {
        this.schedulingController = schedulingController;
    }

    /**
     * This listener is responsible for managing the enabled/disabled state of the controller
     * based on the installed license.
     */
    public static class ControllerStateListener implements EventListener
    {
        private ProjectManager projectManager;
        private UserManager userManager;
        private AgentManager agentManager;
        private FatController controller;

        public void handleEvent(Event event)
        {
            if (event instanceof SystemStartedEvent)
            {
                // Ensure that we are correctly wired.  This is necessary because this controller does not
                // appear in a spring file, and so is built by the object factory before its dependencies
                // are available. Moving this outside the FatController will resolve this.
                SpringComponentContext.autowire(this);
            }
            checkLicense();
        }

        private void checkLicense()
        {
            // the type or detail of the event does not matter at this stage.
            if (licensedToBuild())
            {
                controller.enable();
            }
            else
            {
                controller.disable();
            }
        }

        private boolean licensedToBuild()
        {
            // First check we can run (handles eval expiry and illegal upgrades
            // for commercial license) and then ensure we are within our limits.
            License license = LicenseHolder.getLicense();
            return license.canRunVersion(Version.getVersion()) && !license.isExceeded(projectManager.getProjectCount(), agentManager.getAgentCount(), userManager.getUserCount());
        }

        public Class[] getHandledEvents()
        {
            return new Class[]{LicenseExpiredEvent.class, LicenseUpdateEvent.class, SystemStartedEvent.class};
        }

        public void setProjectManager(ProjectManager projectManager)
        {
            this.projectManager = projectManager;
        }

        public void setUserManager(UserManager userManager)
        {
            this.userManager = userManager;
        }

        public void setAgentManager(AgentManager agentManager)
        {
            this.agentManager = agentManager;
        }

        public void setController(FatController controller)
        {
            this.controller = controller;
        }
    }
}
