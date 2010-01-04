package com.zutubi.pulse.master.scm.polling;

import com.zutubi.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * A queue of polling requests.
 */
public class PollingQueue
{
    /**
     * The list of currently queued requests.
     */
    protected LinkedList<PollingRequest> queuedRequests;

    /**
     * The list of currently activated requests.
     */
    protected LinkedList<PollingRequest> activatedRequests;

    private PollingActivationListener listener;

    public PollingQueue()
    {
        this.queuedRequests = new LinkedList<PollingRequest>();
        this.activatedRequests = new LinkedList<PollingRequest>();
    }

    public void setListener(PollingActivationListener listener)
    {
        this.listener = listener;
    }

    public synchronized List<PollingRequest> enqueue(PollingRequest... requests)
    {
        List<PollingRequest> enqueued = new LinkedList<PollingRequest>();
        for (PollingRequest request : requests)
        {
                queuedRequests.add(0, request);
                enqueued.add(request);
        }

        activateWhatWeCan();

        return enqueued;
    }

    public synchronized List<PollingRequest> dequeue(PollingRequest... requests)
    {
        List<PollingRequest> dequeued = new LinkedList<PollingRequest>();
        for (PollingRequest request : requests)
        {
            if (queuedRequests.contains(request))
            {
                queuedRequests.remove(request);
                dequeued.add(request);
            }
        }

        activateWhatWeCan();

        return dequeued;
    }

    public synchronized List<PollingRequest> complete(PollingRequest... requests)
    {
        List<PollingRequest> completed = new LinkedList<PollingRequest>();
        for (PollingRequest request : requests)
        {
            if (activatedRequests.contains(request))
            {
                activatedRequests.remove(request);
                completed.add(request);
            }
        }

        activateWhatWeCan();

        return completed;
    }

    public synchronized PollingQueueSnapshot getSnapshot()
    {
        return new PollingQueueSnapshot(
                new LinkedList<PollingRequest>(queuedRequests),
                new LinkedList<PollingRequest>(activatedRequests)
        );
    }

    /**
     * Get the list of queued requests.
     *
     * @return the list of queued requests.
     */
    public synchronized List<PollingRequest> getQueuedRequests()
    {
        return getSnapshot().getQueuedRequests();
    }

    /**
     * Get the list of activated requests.
     *
     * @return the list of activated requests.
     */
    public synchronized List<PollingRequest> getActivatedRequests()
    {
        return getSnapshot().getActivatedRequests();
    }

    public synchronized boolean hasRequests()
    {
        return queuedRequests.size() > 0 || activatedRequests.size() > 0;
    }

    private void activateWhatWeCan()
    {
        List<PollingRequest> queueSnapshot = new LinkedList<PollingRequest>();
        queueSnapshot.addAll(queuedRequests);

        for (PollingRequest queuedRequest : CollectionUtils.reverse(queueSnapshot))
        {
            if (queuedRequest.satisfied())
            {
                queuedRequests.remove(queuedRequest);
                activatedRequests.add(0, queuedRequest);
                listener.onActivation(queuedRequest);
            }
        }
    }
}
