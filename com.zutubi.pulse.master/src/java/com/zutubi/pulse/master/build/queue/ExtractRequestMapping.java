package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.util.Mapping;

/**
 * A simple mapping that extracts the build request event from a request holder instance.
 * 
 * @param <T>
 */
public class ExtractRequestMapping<T extends RequestHolder> implements Mapping<T, BuildRequestEvent>
{
    public BuildRequestEvent map(RequestHolder queuedRequest)
    {
        return queuedRequest.getRequest();
    }
}
