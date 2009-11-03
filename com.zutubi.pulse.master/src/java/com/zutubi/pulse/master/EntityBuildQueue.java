package com.zutubi.pulse.master;

import com.zutubi.events.EventManager;
import com.zutubi.events.PublishFlag;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.events.build.AbstractBuildRequestEvent;
import com.zutubi.pulse.master.events.build.BuildActivatedEvent;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.StringUtils;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages build requests and activation of builds for a single entity
 * (project or user).  Responsible for:
 * <ul>
 *     <li>Ensuring earlier requests are serviced first (queueing).</li>
 *     <li>Limiting the number of concurrent builds for the entity.</li>
 *     <li>Replacing existing replaceable requests with new ones from the
 *         same source.</li>
 * </ul>
 */
public class EntityBuildQueue
{
    private static final Logger LOG = Logger.getLogger(EntityBuildQueue.class);
    private static final Messages I18N = Messages.getInstance(EntityBuildQueue.class);

    /**
     * Owner of all builds in this queue, a project or a user (the latter for
     * personal builds).
     */
    private Entity owner;
    /**
     * Maximum number of concurrently-active builds for our owner.
     */
    private int maxActive;
    /**
     * All active builds, now in the hands of a build controller.  Note these
     * builds may not have yet commenced (this happens when the first recipe
     * is assigned).
     */
    private List<ActiveBuild> activeBuilds = new LinkedList<ActiveBuild>();
    /**
     * Queue of builds that have not yet become active, newest first.
     */
    private List<AbstractBuildRequestEvent> queuedBuilds = new LinkedList<AbstractBuildRequestEvent>();
    private volatile boolean stopped = false;

    private ObjectFactory objectFactory;
    private MasterConfigurationManager configurationManager;
    private AccessManager accessManager;
    private EventManager eventManager;
    private BuildRequestRegistry buildRequestRegistry;

    /**
     * Creates a new queue for the given owner.
     *
     * @param owner     the owner (project or user) that we are managing the
     *                  queue for
     * @param maxActive the maximum number of concurrent active builds allowed
     *                  for this owner
     */
    public EntityBuildQueue(Entity owner, int maxActive)
    {
        this.owner = owner;
        this.maxActive = maxActive;
    }

    /**
     * Enqueues a new build request event.  The request must be for the owner
     * that this queue is associated with.  If the request can replace an
     * existing request, the existing request is updated.  Otherwise, the new
     * request is added to the queue.  If this queue is not at the active
     * limit, the request will be activated immediately.
     *
     * @param event the build request to enqueue
     * @throws IllegalArgumentException if the event's owner does not match the
     *                                  queue's
     */
    public void handleRequest(AbstractBuildRequestEvent event)
    {
        if (!event.getOwner().equals(owner))
        {
            buildRequestRegistry.requestRejected(event, I18N.format("rejected.illegal.request"));
            throw new IllegalArgumentException("Attempt to enqueue a build request for a different owner");
        }

        if (updateExistingReplaceableRequest(event))
        {
            return;
        }

        queue(event);
    }

    /**
     * Notifes this queue that a build has completed, and thus should no longer
     * be considered active.
     *
     * @param id the build result id (<strong>not</strong> number)
     */
    public void handleBuildCompleted(long id)
    {
        boolean found = false;
        Iterator<ActiveBuild> it = activeBuilds.iterator();
        while (it.hasNext())
        {
            if (it.next().getController().getBuildId() == id)
            {
                it.remove();
                found = true;
                break;
            }
        }

        if (found && !queuedBuilds.isEmpty())
        {
            activate(queuedBuilds.remove(queuedBuilds.size() - 1));
        }
    }

    /**
     * Cancels a queued build request with the given request event id.  Note
     * that if the build is already active, it will not be cancelled.
     * <p/>
     * The calling thread must have permission to cancel the build:
     * <ul>
     *   <li>Project builds: the user must have cancel permission for the
     *       project</li>
     *   <li>Personal builds: the user must own the build, or be an
     *       administrator</li>
     * </ul>
     * 
     * @param id identifer of the {@link com.zutubi.pulse.master.events.build.AbstractBuildRequestEvent}
     *           to cancel
     * @return true if the request was found and successfully cancelled
     */
    public boolean cancelQueuedRequest(long id)
    {
        Iterator<AbstractBuildRequestEvent> it = queuedBuilds.iterator();
        while (it.hasNext())
        {
            AbstractBuildRequestEvent event = it.next();
            if (event.getId() == id)
            {
                accessManager.ensurePermission(ProjectConfigurationActions.ACTION_CANCEL_BUILD, event);
                it.remove();
                buildRequestRegistry.requestCancelled(event);
                return true;
            }
        }

        return false;
    }
    
    private boolean updateExistingReplaceableRequest(AbstractBuildRequestEvent event)
    {
        // Look for a possibly-replaceable existing request.  Start with the
        // latest queued.
        for (AbstractBuildRequestEvent existing : queuedBuilds)
        {
            if (eventCanReplaceRequest(event, existing))
            {
                existing.setRevision(event.getRevision());
                buildRequestRegistry.requestAssimilated(event, existing.getId());
                return true;
            }
        }

        // Next, see if any active controllers can have their revisions updated.
        for (ActiveBuild activeBuild : activeBuilds)
        {
            if (eventCanReplaceRequest(event, activeBuild.getEvent()))
            {
                try
                {
                    BuildRevision buildRevision = event.getRevision();
                    if (activeBuild.getController().updateRevisionIfNotFixed(buildRevision.getRevision(), buildRevision.getPulseFile()))
                    {
                        buildRequestRegistry.requestAssimilated(event, activeBuild.getEvent().getId());
                        return true;
                    }
                }
                catch (BuildException e)
                {
                    LOG.severe("Unable to update active build revision: " + e.getMessage(), e);
                }
            }
        }

        return false;
    }

    private boolean eventCanReplaceRequest(AbstractBuildRequestEvent event, AbstractBuildRequestEvent existingRequest)
    {
        return existingRequest.isReplaceable() && StringUtils.equals(existingRequest.getRequestSource(), event.getRequestSource());
    }

    private void queue(AbstractBuildRequestEvent event)
    {
        if (activeBuilds.size() < maxActive)
        {
            activate(event);
        }
        else
        {
            queuedBuilds.add(0, event);
            buildRequestRegistry.requestQueued(event);
        }
    }

    private void activate(AbstractBuildRequestEvent event)
    {
        if (stopped)
        {
            // Do not activate any more builds.
            buildRequestRegistry.requestRejected(event, I18N.format("rejected.queue.stopped"));
            return;
        }

        try
        {
            BuildController controller = objectFactory.buildBean(BuildController.class,
                                                                 new Class[] { AbstractBuildRequestEvent.class },
                                                                 new Object[] { event });
            DefaultRecipeResultCollector collector = new DefaultRecipeResultCollector(configurationManager);
            collector.setProjectConfig(event.getProjectConfig());
            controller.setCollector(collector);
            long buildNumber = controller.run();
            activeBuilds.add(0, new ActiveBuild(event, controller));
            buildRequestRegistry.requestActivated(event, buildNumber);

            // Defer this as it must come after a build completed event that
            // we may be handling.
            eventManager.publish(new BuildActivatedEvent(this, event), PublishFlag.DEFERRED);
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }
    }

    public Entity getOwner()
    {
        return owner;
    }

    /**
     * @return the number of active (i.e. build controllers started) builds in
     *         this queue
     */
    public int getActiveBuildCount()
    {
        return activeBuilds.size();
    }

    /**
     * @return a snapshot of all active (i.e. build controllers started) builds
     *         in this queue
     */
    public List<ActiveBuild> getActiveBuildsSnapshot()
    {
        return new LinkedList<ActiveBuild>(activeBuilds);
    }

    /**
     * @return the number of queued (i.e. not yet active) builds in this queue
     */
    public int getQueuedBuildCount()
    {
        return queuedBuilds.size();
    }

    /**
     * @return a snapshot of all queued (i.e. not yet active) builds in this
     *         queue
     */
    public List<AbstractBuildRequestEvent> getQueuedBuildsSnapshot()
    {
        return new LinkedList<AbstractBuildRequestEvent>(queuedBuilds);
    }

    /**
     * Notifies this queue that it should stop activating build requests - that
     * is no more build controllers should be started.
     */
    public void stop()
    {
        stopped = true;
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setAccessManager(AccessManager accessManager)
    {
        this.accessManager = accessManager;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setBuildRequestRegistry(BuildRequestRegistry buildRequestRegistry)
    {
        this.buildRequestRegistry = buildRequestRegistry;
    }

    /**
     * Small data type to pair a build request with the active controller for
     * that build.
     */
    public static class ActiveBuild
    {
        private AbstractBuildRequestEvent event;
        private BuildController controller;

        public ActiveBuild(AbstractBuildRequestEvent event, BuildController controller)
        {
            this.event = event;
            this.controller = controller;
        }

        public AbstractBuildRequestEvent getEvent()
        {
            return event;
        }

        public BuildController getController()
        {
            return controller;
        }
    }
}
