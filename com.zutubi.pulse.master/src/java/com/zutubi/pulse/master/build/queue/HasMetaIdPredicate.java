package com.zutubi.pulse.master.build.queue;

import com.zutubi.util.Predicate;

/**
 * A predicate that matches a request holder containing a
 * build request event with a specified meta id.
 *
 * @param <T> the specific subclass of RequestHolder that is being searched.
 */
public class HasMetaIdPredicate<T extends RequestHolder> implements Predicate<T>
{
    private long metaBuildId;

    public HasMetaIdPredicate(long metaBuildId)
    {
        this.metaBuildId = metaBuildId;
    }

    public boolean satisfied(RequestHolder holder)
    {
        return holder.getMetaBuildId() == metaBuildId;
    }
}
