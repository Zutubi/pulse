package com.zutubi.pulse.master.build.queue;

import com.zutubi.events.EventManager;
import com.zutubi.events.PublishFlag;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.master.build.control.BuildController;
import com.zutubi.pulse.master.build.control.BuildControllerFactory;
import com.zutubi.pulse.master.events.build.BuildActivatedEvent;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The build queue tracks queued and active build requests.
 * <p/>
 * A request remains queued until all of its predicates are satisfied, at which point
 * it is activated.
 */
public class BuildQueue
{
    private static final Messages I18N = Messages.getInstance(BuildQueue.class);

    private BuildControllerFactory buildControllerFactory;
    private BuildRequestRegistry buildRequestRegistry;
    private EventManager eventManager;

    /**
     * The list of currently queued requests.
     */
    private LinkedList<QueuedRequest> queuedRequests = new LinkedList<QueuedRequest>();

    /**
     * The list of activated requests.
     */
    private LinkedList<ActivatedRequest> activatedRequests = new LinkedList<ActivatedRequest>();

    /**
     * When paused, this build queue will suspend activating requests until
     * the queue is unpaused.
     */
    private volatile boolean paused = false;

    /**
     * When stopped, this build queue will stop activating requests.
     */
    private volatile boolean stopped = false;

    /**
     * Enqueue the requests.
     * <p/>
     * Those requests that are placed earlier in the list will be given the opportunity
     * to be activated ahead of those that appear later in the list.
     *
     * @param requests the requests to be enqueued.
     */
    public synchronized void enqueue(List<QueuedRequest> requests)
    {
        for (QueuedRequest request : requests)
        {
            if (onNewlyQueued(request))
            {
                queuedRequests.add(0, request);
                buildRequestRegistry.requestQueued(request.getRequest());
            }
        }

        activateWhatWeCan();
    }

    /**
     * Enqueue the requests.
     *
     * @param requests the requests to enqueue.
     *
     * @see #enqueue(java.util.List)
     */
    public synchronized void enqueue(QueuedRequest... requests)
    {
        enqueue(Arrays.asList(requests));
    }

    /**
     * Cancel the queued request with the specified id.  If no such queued request exists,
     * or if the request has been activated, no change is made and false is returned.
     *
     * @param requestId the id of the request to be cancelled.
     *
     * @return true if a queued request matching the request id was located and cancelled,
     * false otherwise.
     */
    public synchronized boolean cancel(long requestId)
    {
        QueuedRequest requestToCancel = CollectionUtils.find(queuedRequests, new HasIdPredicate<QueuedRequest>(requestId));

        if (requestToCancel != null)
        {
            queuedRequests.remove(requestToCancel);
            buildRequestRegistry.requestCancelled(requestToCancel.getRequest());

            activateWhatWeCan();
        }

        return requestToCancel != null;
    }

    /**
     * Complete the activated request with the specified id.  If no such activated request exists,
     * or if the request has is still queued, no change is made and false is returned.
     *
     * @param requestId the id of the request to be completed
     *
     * @return true if an activated request matching the request is was located and completed,
     * false otherwise.
     */
    public synchronized boolean complete(long requestId)
    {
        ActivatedRequest completedRequest = CollectionUtils.find(activatedRequests, new HasIdPredicate<ActivatedRequest>(requestId));

        if (completedRequest != null)
        {
            activatedRequests.remove(completedRequest);

            activateWhatWeCan();
        }

        return completedRequest != null;
    }

    /**
     * Stop the queue from activating any further requests.  This is
     * permanent.
     *
     * To temporarily stop the activation of requests, see {@link #pauseActivation()}
     */
    public synchronized void stop()
    {
        stopped = true;
    }

    /**
     * Pause the activation of the build queue.  This will allow multiple related
     * actions to be taken without for accidental activation of a request.
     * <p/>
     * Important note.  To ensure that access to this queue is thread safe during
     * the extended processing, make sure that you synchronise on this instance.
     * <p/>
     * For example:
     * <code>
     * synchronized(buildQueue)
     * {
     * try
     * {
     * buildQueue.pauseActivation();
     * buildQueue.cancel(request1);
     * buildQueue.cancel(request2);
     * ....
     * }
     * finally
     * {
     * buildQueue.resumeActivation();
     * }
     * }
     * </code>
     *
     * @throws IllegalStateException if the current thread does not hold a lock on the build queue.
     * @throws IllegalArgumentException if the build queue is already paused.
     */
    public void pauseActivation()
    {
        if (!Thread.holdsLock(this))
        {
            throw new IllegalStateException(I18N.format("queue.lock.required"));
        }

        if (paused)
        {
            throw new IllegalStateException(I18N.format("queue.already.paused"));
        }

        this.paused = true;
    }

    /**
     * Unpause the activation of this build queue.
     *
     * @throws IllegalStateException if the current thread does not hold a lock on the build queue.
     * @throws IllegalArgumentException if the build queue is not already paused.
     *
     * @see #pauseActivation()
     */
    public void resumeActivation()
    {
        if (!Thread.holdsLock(this))
        {
            throw new IllegalStateException(I18N.format("queue.lock.required"));
        }

        if (!paused)
        {
            throw new IllegalStateException(I18N.format("queue.not.paused"));
        }

        this.paused = false;

        activateWhatWeCan();
    }

    private synchronized void activateWhatWeCan()
    {
        // activation has been disabled either temporarily or permanently
        if (paused || stopped)
        {
            return;
        }

        List<ActivatedRequest> toActivateRequests = new LinkedList<ActivatedRequest>();
        List<QueuedRequest> queueSnapshot = new LinkedList<QueuedRequest>();
        queueSnapshot.addAll(queuedRequests);

        // We need to update the queuedRequest and activatedRequest lists as we go to
        // ensure that subsequent .satisfied() checks have an accurate state to work with.
        for (QueuedRequest queuedRequest : CollectionUtils.reverse(queueSnapshot))
        {
            if (queuedRequest.satisfied())
            {
                queuedRequests.remove(queuedRequest);

                ActivatedRequest activatedRequest = new ActivatedRequest(queuedRequest.getRequest());
                activatedRequests.add(0, activatedRequest);
                toActivateRequests.add(activatedRequest);
            }
        }

        // Now we go through and finish activating the newly activated requests.
        for (ActivatedRequest activatedRequest : toActivateRequests)
        {
            onNewlyActivated(activatedRequest);

            // it may be worth taking this startup processing outside the synchronisation block since it may take some time.
            BuildController controller = buildControllerFactory.create(activatedRequest.getRequest());
            long buildNumber = controller.start();

            activatedRequest.setController(controller);

            buildRequestRegistry.requestActivated(activatedRequest.getRequest(), buildNumber);
            eventManager.publish(new BuildActivatedEvent(this, activatedRequest.getRequest()), PublishFlag.DEFERRED);
        }
    }

    /**
     * The assimilation processing is delayed to as late as possible to avoid assimilating into
     * a build request that is later cancelled.  As such, we only assimilate into activated requests.
     * <p/>
     * When we receive a new queued request, check if we can assimilate into any of the currently
     * activated requests.
     *
     * @param queuedRequest the request to be compared to the existing activated requests for
     *                      assimilation.
     * @return true if we can continue to queue this request, false otherwise.
     */
    private boolean onNewlyQueued(final QueuedRequest queuedRequest)
    {
        // find the first activated request that we can assimilate into.
        BuildRequestEvent event = queuedRequest.getRequest();
        List<ActivatedRequest> assimilationCandidates = CollectionUtils.filter(activatedRequests,
                new HasOwnerAndSource<ActivatedRequest>(event.getOwner(), event.getOptions().getSource())
        );
        if (assimilationCandidates.size() > 0)
        {
            ActivatedRequest activatedRequest = null;
            for (ActivatedRequest request : assimilationCandidates)
            {
                // is revision fixed?
                BuildRevision buildRevision = queuedRequest.getRequest().getRevision();
                if (request.getController().updateRevisionIfNotFixed(buildRevision.getRevision()))
                {
                    activatedRequest = request;
                    break;
                }
            }

            // if we located an active request we can assimilate with, do so.
            if (activatedRequest != null)
            {
                buildRequestRegistry.requestAssimilated(queuedRequest.getRequest(), activatedRequest.getRequest().getId());
                return false;
            }
        }
        return true;
    }

    private void onNewlyActivated(final ActivatedRequest activated)
    {
        // on newly assimilated, it is like two queued items merging, so we keep it simple and do not need to
        // include any crazy business with the controller.

        if (activated.getRequest().getOptions().isReplaceable())
        {
            List<QueuedRequest> assimilationCandidates = CollectionUtils.filter(queuedRequests, new Predicate<QueuedRequest>()
            {
                public boolean satisfied(QueuedRequest queued)
                {
                    return queued.getRequest().getOwner().equals(activated.getRequest().getOwner()) &&
                            StringUtils.equals(activated.getRequest().getOptions().getSource(), queued.getRequest().getOptions().getSource());
                }
            });

            if (assimilationCandidates.size() > 0)
            {
                BuildRequestEvent latestQueuedRequest = assimilationCandidates.get(0).getRequest();
                activated.getRequest().setRevision(latestQueuedRequest.getRevision());

                for (QueuedRequest assimilationTarget : assimilationCandidates)
                {
                    buildRequestRegistry.requestAssimilated(assimilationTarget.getRequest(), activated.getRequest().getId());
                    queuedRequests.remove(assimilationTarget);
                }
            }
        }
    }

    /**
     * Get the list of queued requests.
     *
     * @return the list of queued requests.
     */
    public synchronized List<QueuedRequest> getQueuedRequests()
    {
        return new LinkedList<QueuedRequest>(queuedRequests);
    }

    /**
     * Retrieve all of the queued requests that belong to the specified owner.
     *
     * @param owner the owner of the queued requests.
     * @return a list of queued requests belonging to the specified owner.
     */
    public synchronized List<QueuedRequest> getQueuedRequestsByOwner(Object owner)
    {
        return CollectionUtils.filter(queuedRequests, new HasOwnerPredicate<QueuedRequest>(owner));
    }

    /**
     * Get the list of activated requests.
     *
     * @return the list of activated requests.
     */
    public synchronized List<ActivatedRequest> getActivatedRequests()
    {
        return new LinkedList<ActivatedRequest>(activatedRequests);
    }

    /**
     * Retrieve all of teh activated requests that belong to the specified owner.
     *
     * @param owner the owner of the activated requests.
     * @return a list of activated requests belonging to the specified owner.
     */
    public synchronized List<ActivatedRequest> getActivatedRequestsByOwner(Object owner)
    {
        return CollectionUtils.filter(activatedRequests, new HasOwnerPredicate<ActivatedRequest>(owner));
    }

    /**
     * Return a count of the number of actived requests.
     *
     * @return the number of activated requests.
     */
    public synchronized int getActivatedRequestCount()
    {
        return getActivatedRequests().size();
    }

    /**
     * Get the list of requests, both activated and queued, that are
     * associated with the specified meta build id.
     *
     * @param metaBuildId the meta build id identifying the requests to be retrieved.
     * @return a list of requests associated with the specified meta build id.
     */
    public synchronized List<RequestHolder> getMetaBuildRequests(long metaBuildId)
    {
        LinkedList<RequestHolder> requests = new LinkedList<RequestHolder>();
        requests.addAll(CollectionUtils.filter(queuedRequests, new HasMetaIdPredicate<QueuedRequest>(metaBuildId)));
        requests.addAll(CollectionUtils.filter(activatedRequests, new HasMetaIdPredicate<ActivatedRequest>(metaBuildId)));
        return requests;
    }

    /**
     * Get the identified request.
     *
     * @param requestId the identifier of the request to be retrieved.
     *
     * @return the build request event matching the id.
     */
    public synchronized BuildRequestEvent getRequest(long requestId)
    {
        RequestHolder request = CollectionUtils.find(queuedRequests, new HasIdPredicate<QueuedRequest>(requestId));
        if (request == null)
        {
            request = CollectionUtils.find(activatedRequests, new HasIdPredicate<ActivatedRequest>(requestId));
        }
        if (request != null)
        {
            return request.getRequest();
        }
        return null;
    }

    /**
     * Returns true if the specified owner has an active or queued request.
     *
     * @param owner the owner of the request
     *
     * @return true if the specified owner has a request in the queue, false otherwise
     */
    public synchronized boolean hasRequest(Object owner)
    {
        return getRequestsByOwner(owner).size() > 0;
    }

    /**
     * Returns a list of build requests for the specified owner.
     *
     * @param owner the owner of the request
     *
     * @return a list of build request event instances associated with the specified owner.
     */
    public synchronized List<BuildRequestEvent> getRequestsByOwner(Object owner)
    {
        List<BuildRequestEvent> byOwner = new LinkedList<BuildRequestEvent>();
        byOwner.addAll(CollectionUtils.map(CollectionUtils.filter(queuedRequests, new HasOwnerPredicate<QueuedRequest>(owner)), new ExtractRequestMapping<QueuedRequest>()));
        byOwner.addAll(CollectionUtils.map(CollectionUtils.filter(activatedRequests, new HasOwnerPredicate<ActivatedRequest>(owner)), new ExtractRequestMapping<ActivatedRequest>()));
        return byOwner;
    }

    /**
     * Get a snapshot of the internal state of the build queue as it is right now.
     *
     * @return the queue snapshot.
     */
    public synchronized BuildQueueSnapshot getSnapshot()
    {
        BuildQueueSnapshot snapshot = new BuildQueueSnapshot();
        snapshot.addAllActivatedRequests(activatedRequests);
        snapshot.addAllQueuedRequests(queuedRequests);
        return snapshot;
    }

    public void setBuildRequestRegistry(BuildRequestRegistry buildRequestRegistry)
    {
        this.buildRequestRegistry = buildRequestRegistry;
    }

    public void setBuildControllerFactory(BuildControllerFactory buildControllerFactory)
    {
        this.buildControllerFactory = buildControllerFactory;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}
