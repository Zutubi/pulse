package com.zutubi.pulse.master.scm.util;

/**
 * A convenience implementation of the PredicateRequestQueueListener that allows
 * implementations to extend it without having to implement all of the listeners
 * interface.
 */
public class PredicateRequestQueueListenerAdapter<T> implements PredicateRequestQueueListener<T>
{
    public boolean onEnqueue(PredicateRequest<T> tPredicateRequest)
    {
        return true;
    }

    public boolean onDequeue(PredicateRequest<T> tPredicateRequest)
    {
        return true;
    }

    public boolean onActivation(PredicateRequest<T> tPredicateRequest)
    {
        return true;
    }

    public boolean onCompletion(PredicateRequest<T> tPredicateRequest)
    {
        return true;
    }
}
