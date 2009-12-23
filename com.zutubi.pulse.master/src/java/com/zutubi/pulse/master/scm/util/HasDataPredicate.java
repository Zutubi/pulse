package com.zutubi.pulse.master.scm.util;

import com.zutubi.util.Predicate;

/**
 * A predicate that is satisfied if and only if the predicate request
 * contains the expected data.
 *
 * @param <T>
 */
public class HasDataPredicate<T> implements Predicate<PredicateRequest<T>>
{
    private T data;

    public HasDataPredicate(T data)
    {
        this.data = data;
    }

    public boolean satisfied(PredicateRequest<T> request)
    {
        return request.getData().equals(data);
    }
}