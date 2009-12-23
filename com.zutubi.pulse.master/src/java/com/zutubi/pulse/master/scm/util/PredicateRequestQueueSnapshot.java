package com.zutubi.pulse.master.scm.util;

import java.util.List;
import java.util.Collections;

/**
 * A simple snapshot of the internal state of the queues lists.
 * 
 * @param <T>
 */
public class PredicateRequestQueueSnapshot<T>
{
    private long timestamp;

    private final List<PredicateRequest<T>> queuedRequests;
    private final List<PredicateRequest<T>> activatedRequests;

    public PredicateRequestQueueSnapshot(List<PredicateRequest<T>> queuedRequests, List<PredicateRequest<T>> activatedRequests)
    {
        this.queuedRequests = queuedRequests;
        this.activatedRequests = activatedRequests;

        timestamp = System.currentTimeMillis(); // replace with Clock.
    }

    public List<PredicateRequest<T>> getQueuedRequests()
    {
        return Collections.unmodifiableList(queuedRequests);
    }

    public List<PredicateRequest<T>> getActivatedRequests()
    {
        return Collections.unmodifiableList(activatedRequests);
    }

    public long getTimestamp()
    {
        return timestamp;
    }
}
