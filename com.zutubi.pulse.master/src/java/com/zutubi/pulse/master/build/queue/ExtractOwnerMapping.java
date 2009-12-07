package com.zutubi.pulse.master.build.queue;

import com.zutubi.util.Mapping;

/**
 * A mapping from a request holder to the owner of the request.
 *
 * @param <T>
 */
public class ExtractOwnerMapping<T extends RequestHolder> implements Mapping<T, Object>
{
    public Object map(RequestHolder queuedRequest)
    {
        return queuedRequest.getOwner();
    }
}