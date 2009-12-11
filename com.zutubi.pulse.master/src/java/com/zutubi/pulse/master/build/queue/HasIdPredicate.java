package com.zutubi.pulse.master.build.queue;

import com.zutubi.util.Predicate;

/**
 * A predicate that matches a request holder containing a
 * build request event with a specified id.
 *
 * @param <T> the specific subclass of RequestHolder that is being searched.
 */
public class HasIdPredicate<T extends RequestHolder> implements Predicate<T>
{
    private long id;

    public HasIdPredicate(long id)
    {
        this.id = id;
    }

    public boolean satisfied(T holder)
    {
        return holder.getRequest().getId() == id;
    }
}
