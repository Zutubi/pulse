package com.zutubi.pulse.master.scm.polling;

import java.util.List;
import java.util.Collections;

/**
 * A simple snapshot of the internal state of the queues lists.
 */
public class PollingQueueSnapshot
{
    private final List<PollingRequest> queuedRequests;
    private final List<PollingRequest> activatedRequests;

    public PollingQueueSnapshot(List<PollingRequest> queuedRequests, List<PollingRequest> activatedRequests)
    {
        this.queuedRequests = queuedRequests;
        this.activatedRequests = activatedRequests;
    }

    /**
     * The list of queued requests.
     *
     * @return the immutable list of queued requests.
     */
    public List<PollingRequest> getQueuedRequests()
    {
        return Collections.unmodifiableList(queuedRequests);
    }

    /**
     * The list of activated requests.
     *
     * @return the immutable list of activated requests.
     */
    public List<PollingRequest> getActivatedRequests()
    {
        return Collections.unmodifiableList(activatedRequests);
    }
}
