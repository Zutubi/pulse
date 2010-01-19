package com.zutubi.pulse.master.build.queue;

import com.zutubi.util.Predicate;
import com.zutubi.pulse.core.BuildRevision;

/**
 * This predicate is satisfied by any requests that have a fixed revision.
 *
 * @see BuildRevision#isFixed() 
 */
public class HasFixedRevisionPredicate<T extends RequestHolder> implements Predicate<T>
{
    public boolean satisfied(T t)
    {
        return t.getRequest().getRevision().isFixed();
    }
}
