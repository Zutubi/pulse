package com.zutubi.pulse.master.build.queue;

import com.google.common.base.Predicate;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.util.StringUtils;

/**
 * A predicate that matches a request holder containing a
 * build request event with a specified owner and options source field.
 *
 * @param <T> the specific subclass of RequestHolder that is being searched.
 */
public class HasOwnerAndSourcePredicate<T extends RequestHolder> implements Predicate<T>
{
    private Object owner;
    private String source;

    public HasOwnerAndSourcePredicate(RequestHolder request)
    {
        this(request.getOwner(), request.getRequest().getRequestSource());
    }

    public HasOwnerAndSourcePredicate(Object owner, String source)
    {
        this.owner = owner;
        this.source = source;
    }

    public boolean apply(T holder)
    {
        BuildRequestEvent request = holder.getRequest();
        return request.getOwner().equals(owner) && StringUtils.equals(request.getOptions().getSource(), source);
    }
}
