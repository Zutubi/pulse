package com.zutubi.pulse.master.scm.util;

/**
 * A callback interface that provides notifications of a requests
 * state change within the queue.  Each methods returns a boolean
 * that can be used to veto the change. 
 */
public interface PredicateRequestQueueListener<T>
{
    /**
     * Called when the request is initially enqueued.
     *
     * @param request the request being enqueued.
     *
     * @return true if the request can be enqueued, false if it should be
     * dropped.
     */
    boolean onEnqueue(PredicateRequest<T> request);

    /**
     * Called when a request is about to be removed from the
     * queue, prior to it being activated.
     *
     * @param request   the request about to be dequeued.
     *
     * @return true if this request can be dequeued, false if it should
     * remain in the queue.
     */
    boolean onDequeue(PredicateRequest<T> request);

    /**
     * Called when a request is about to be activated.
     *
     * @param request   the request to be activated.
     *
     * @return true if this request can be activated, false if it should
     * remain in the queue.
     */
    boolean onActivation(PredicateRequest<T> request);

    /**
     * Called when a request is about to be completed and hence
     * removed from the queued activated list.
     *
     * @param request   the request to be completed.
     *
     * @return true if this request can be completed and removed from
     * the queue, false if it should remain in the activated state.
     */
    boolean onCompletion(PredicateRequest<T> request);
}
