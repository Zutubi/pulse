package com.zutubi.pulse.master.scm.util;

import com.zutubi.util.CollectionUtils;
import com.zutubi.i18n.Messages;

import java.util.LinkedList;
import java.util.List;

/**
 * A queue like structure in that items can be enqueued and posses
 * a dynamic ordering based on the requests predicates.  Once an enqueued
 * requests predicates are satisfied, it is marked as activated until
 * completed (dequeued).
 */
public class PredicateRequestQueue<T>
{
    private static final Messages I18N = Messages.getInstance(PredicateRequestQueue.class);

    private LinkedList<PredicateRequest<T>> queuedRequests;
    private LinkedList<PredicateRequest<T>> activatedRequests;

    private PredicateRequestQueueListener<T> listener;

    private boolean activationPaused = false;

    public PredicateRequestQueue()
    {
        this.queuedRequests = new LinkedList<PredicateRequest<T>>();
        this.activatedRequests = new LinkedList<PredicateRequest<T>>();
    }

    public void setListener(PredicateRequestQueueListener<T> listener)
    {
        this.listener = listener;
    }

    public synchronized void enqueue(PredicateRequest<T>... requests)
    {
        for (PredicateRequest<T> request : requests)
        {
            if (listener.onEnqueue(request))
            {
                queuedRequests.add(0, request);
            }
        }

        activateWhatWeCan();
    }

    public synchronized List<PredicateRequest<T>> dequeue(PredicateRequest<T>... requests)
    {
        List<PredicateRequest<T>> dequeued = new LinkedList<PredicateRequest<T>>();
        for (PredicateRequest<T> request : requests)
        {
            if (queuedRequests.contains(request) && listener.onDequeue(request))
            {
                queuedRequests.remove(request);
                dequeued.add(request);
            }
        }

        activateWhatWeCan();

        return dequeued;
    }

    public synchronized List<PredicateRequest<T>> complete(PredicateRequest<T>... requests)
    {
        List<PredicateRequest<T>> completed = new LinkedList<PredicateRequest<T>>();
        for (PredicateRequest<T> request : requests)
        {
            if (activatedRequests.contains(request) && listener.onCompletion(request))
            {
                activatedRequests.remove(request);
                completed.add(request);
            }
        }

        activateWhatWeCan();

        return completed;
    }

    public void pauseActivation()
    {
        if (!Thread.holdsLock(this))
        {
            throw new IllegalStateException(I18N.format("queue.lock.required"));
        }

        if (activationPaused)
        {
            throw new IllegalStateException(I18N.format("queue.already.paused"));
        }

        this.activationPaused = true;
    }

    public void resumeActivation()
    {
        if (!Thread.holdsLock(this))
        {
            throw new IllegalStateException(I18N.format("queue.lock.required"));
        }

        if (!activationPaused)
        {
            throw new IllegalStateException(I18N.format("queue.not.paused"));
        }

        this.activationPaused = false;

        activateWhatWeCan();
    }

    public synchronized PredicateRequestQueueSnapshot<T> getSnapshot()
    {
        return new PredicateRequestQueueSnapshot<T>(
                new LinkedList<PredicateRequest<T>>(queuedRequests),
                new LinkedList<PredicateRequest<T>>(activatedRequests)
        );
    }

    public synchronized boolean hasRequests()
    {
        return queuedRequests.size() > 0 || activatedRequests.size() > 0;
    }

    private void activateWhatWeCan()
    {
        if (activationPaused)
        {
            return;
        }

        List<PredicateRequest<T>> queueSnapshot = new LinkedList<PredicateRequest<T>>();
        queueSnapshot.addAll(queuedRequests);

        for (PredicateRequest<T> queuedRequest : CollectionUtils.reverse(queueSnapshot))
        {
            if (queuedRequest.satisfied())
            {
                if (listener.onActivation(queuedRequest))
                {
                    queuedRequests.remove(queuedRequest);
                    activatedRequests.add(0, queuedRequest);
                }
            }
        }

        // A sanity check: We should never have an empty activated list when we have
        // items in the queued list.  This indicates a request that can not be activated
        // (circular predicate dependency maybe..)
        if (queuedRequests.size() > 0 && activatedRequests.size() == 0)
        {
            throw new IllegalStateException();
        }
    }

}
