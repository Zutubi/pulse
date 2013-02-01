package com.zutubi.pulse.master.build.queue;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.util.CollectionUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The build snapshot is an immutable snapshot of the contents of the build queue
 * at the time it was requested.
 */
public class BuildQueueSnapshot
{
    private List<ActivatedRequest> activatedRequests = new LinkedList<ActivatedRequest>();
    private List<QueuedRequest> queuedRequests = new LinkedList<QueuedRequest>();

    /**
     * The time at which the snapshot was taken (pulse server time).
     */
    private long timestamp;

    public BuildQueueSnapshot()
    {
        timestamp = System.currentTimeMillis();
    }

    /**
     * The list of active build requests.
     *
     * @return the immutable list of active build requests.
     */
    public List<BuildRequestEvent> getActivatedBuildRequests()
    {
        return Collections.unmodifiableList(CollectionUtils.map(activatedRequests, new ExtractRequestFunction<ActivatedRequest>()));
    }

    /**
     * The list of activated requests.
     *
     * @return the immutable list of activated requests.
     */
    public List<ActivatedRequest> getActivatedRequests()
    {
        return Collections.unmodifiableList(activatedRequests);
    }

    /**
     * The list of queued build requests.
     *
     * @return the immutable list of queued build requests.
     */
    public List<BuildRequestEvent> getQueuedBuildRequests()
    {
        return Collections.unmodifiableList(CollectionUtils.map(queuedRequests, new ExtractRequestFunction<QueuedRequest>()));
    }

    /**
     * The list of queued requests.
     *
     * @return the immutable list of queued requests.
     */
    public List<QueuedRequest> getQueuedRequests()
    {
        return Collections.unmodifiableList(queuedRequests);
    }

    /**
     * The list of queued requests for the specified owner.
     *
     * @param owner     the owner of the requests to be returned.
     * @return the immutable list of queued requests for the specified owner.
     */
    public List<QueuedRequest> getQueuedRequestsByOwner(Object owner)
    {
        return Lists.newArrayList(Iterables.filter(queuedRequests, new HasOwnerPredicate<QueuedRequest>(owner)));
    }

    /**
     * Get the time at which the snapshot was taken.
     *
     * @return a timestamp of when the snapshot was taken, in
     * milliseconds since epoch.
     */
    public long getTimestamp()
    {
        return timestamp;
    }

    public void addAllQueuedRequests(LinkedList<QueuedRequest> queuedRequests)
    {
        this.queuedRequests.addAll(queuedRequests);
    }

    public void addAllActivatedRequests(LinkedList<ActivatedRequest> activatedRequests)
    {
        this.activatedRequests.addAll(activatedRequests);
    }
}
