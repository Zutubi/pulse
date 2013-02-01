package com.zutubi.pulse.master.build.queue;

import com.google.common.base.Function;

/**
 * A mapping from a request holder to the owner of the request.
 *
 * @param <T>
 */
public class ExtractOwnerFunction<T extends RequestHolder> implements Function<T, Object>
{
    public Object apply(RequestHolder queuedRequest)
    {
        return queuedRequest.getOwner();
    }
}