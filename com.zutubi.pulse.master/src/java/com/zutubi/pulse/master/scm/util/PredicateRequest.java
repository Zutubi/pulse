package com.zutubi.pulse.master.scm.util;

import com.zutubi.util.Predicate;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

/**
 * The predicate request represents an entry in the PredicateRequestQueue.  It
 * contains a single item of data and a list of predicates.  This request will
 * not be activated by the queue until all of its predicates are satisfied.
 */
public class PredicateRequest<T>
{
    /**
     * The data to be queued in the predicate queue, whose activation is
     * controlled by the listed predicates.
     */
    private T data;
    
    private List<Predicate<PredicateRequest<T>>> predicates;

    public PredicateRequest(T data, Predicate<PredicateRequest<T>>... predicates)
    {
        this(data, Arrays.asList(predicates));
    }

    public PredicateRequest(T data, List<Predicate<PredicateRequest<T>>> predicates)
    {
        this.data = data;
        this.predicates = new LinkedList<Predicate<PredicateRequest<T>>>(predicates);
    }

    /**
     * Add the provided predicate to the list of predicates for this request.
     *
     * @param predicate a predicate
     */
    public void add(Predicate<PredicateRequest<T>> predicate)
    {
        this.predicates.add(predicate);
    }

    public T getData()
    {
        return data;
    }

    /**
     * Returns true if and only if all of this requests predicates are
     * satisfied.
     *
     * @return true if the predicates are satisfied, false otherwise.
     */
    public boolean satisfied()
    {
        for (Predicate<PredicateRequest<T>> predicate : predicates)
        {
            if (!predicate.satisfied(this))
            {
                return false;
            }
        }
        return true;
    }
}
