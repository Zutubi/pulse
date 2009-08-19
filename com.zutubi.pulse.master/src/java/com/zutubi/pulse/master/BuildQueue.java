package com.zutubi.pulse.master;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.NamedEntity;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The build queue manages {@link EntityBuildQueue} instances for each
 * project and user.  As build requests come in they are sent to the
 * appropriate queue.
 */
public class BuildQueue
{
    private static final Logger LOG = Logger.getLogger(BuildQueue.class);

    /**
     * Map from entity to a specific queue for that entity.  The entity is
     * either a project or a user (for personal builds).
     */
    private Map<NamedEntity, EntityBuildQueue> entityQueues = new HashMap<NamedEntity, EntityBuildQueue>();
    private boolean stopped = false;

    private ObjectFactory objectFactory;

    /**
     * Adds the request to the queue.  The request will be activated if the
     * number of active builds for the owner is not yet at the limit.
     *
     * @param event the request to add
     */
    public void buildRequested(BuildRequestEvent event)
    {
        if (!stopped)
        {
            lookupQueueForOwner(event.getOwner()).handleRequest(event);
        }
    }

    /**
     * Notifies the queue that a build has completed.  The build is removed
     * from the active builds.
     *
     * @param owner   owner (project or user) of the build
     * @param buildId the build result id (<strong>not</strong> the number)
     */
    public void buildCompleted(NamedEntity owner, long buildId)
    {
        lookupQueueForOwner(owner).handleBuildCompleted(buildId);
    }

    private EntityBuildQueue lookupQueueForOwner(NamedEntity owner)
    {
        EntityBuildQueue queue = entityQueues.get(owner);
        if (queue == null)
        {
            queue = objectFactory.buildBean(EntityBuildQueue.class, new Class[] { Entity.class, Integer.TYPE }, new Object[] { owner, 1 });
            entityQueues.put(owner, queue);
        }

        return queue;
    }

    /**
     * Lookup the number of active builds for an entity.
     *
     * @param owner the entity to look up by
     * @return the number of active builds for the given entity
     */
    public int getActiveBuildCount(NamedEntity owner)
    {
        return lookupQueueForOwner(owner).getActiveBuildCount();
    }

    /**
     * @return the total number of active builds for all owners
     */
    public int getActiveBuildCount()
    {
        int total = 0;
        for (EntityBuildQueue queue: entityQueues.values())
        {
            total += queue.getActiveBuildCount();
        }

        return total;
    }

    /**
     * Returns a consistent view of this queue at a point in time.
     *
     * @see Snapshot
     *
     * @return the current state of the quue
     */
    public Snapshot takeSnapshot()
    {
        Snapshot snapshot = new Snapshot();
        for (EntityBuildQueue queue: entityQueues.values())
        {
            snapshot.addEntityQueue(queue);
        }

        return snapshot;
    }

    /**
     * Cancels a queued build by the request event id.  If the build is already
     * active, or no longer queued, this is a no-op.
     *
     * @param requestEventId event identifier of the request to cancel
     * @return true if the request was found and cancelled, false if it was not
     *         found or already active
     */
    public boolean cancelBuild(long requestEventId)
    {
        boolean found = false;
        for (EntityBuildQueue queue: entityQueues.values())
        {
            if (queue.cancelQueuedRequest(requestEventId))
            {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Notifies this queue that we are stopping, so no further requests should
     * be accepted or activated.
     */
    public void stop()
    {
        this.stopped = true;
        for (EntityBuildQueue queue: entityQueues.values())
        {
            queue.stop();
        }
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    /**
     * Used to hold a consistent snapshot across all queues.
     */
    public static class Snapshot
    {
        private Map<Entity, List<EntityBuildQueue.ActiveBuild>> activeBuilds = new HashMap<Entity, List<EntityBuildQueue.ActiveBuild>>();
        private Map<Entity, List<BuildRequestEvent>> queuedBuilds = new HashMap<Entity, List<BuildRequestEvent>>();

        void addEntityQueue(EntityBuildQueue queue)
        {
            activeBuilds.put(queue.getOwner(), queue.getActiveBuildsSnapshot());
            queuedBuilds.put(queue.getOwner(), queue.getQueuedBuildsSnapshot());
        }

        /**
         * @return a map from an entity (project or user) to the active builds
         * for that entity.  Builds activate first are last in the list.
         */
        public Map<Entity, List<EntityBuildQueue.ActiveBuild>> getActiveBuilds()
        {
            return Collections.unmodifiableMap(activeBuilds);
        }

        /**
         * @return a map from an entity (project or user) to the queued build
         * requests for that entity.  Builds requested first are last in the
         * list.
         */
        public Map<Entity, List<BuildRequestEvent>> getQueuedBuilds()
        {
            return Collections.unmodifiableMap(queuedBuilds);
        }
    }
}
