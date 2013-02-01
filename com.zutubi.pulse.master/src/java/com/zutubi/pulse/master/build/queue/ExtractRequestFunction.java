package com.zutubi.pulse.master.build.queue;

import com.google.common.base.Function;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;

/**
 * A simple mapping that extracts the build request event from a request holder instance.
 * 
 * @param <T>
 */
public class ExtractRequestFunction<T extends RequestHolder> implements Function<T, BuildRequestEvent>
{
    public BuildRequestEvent apply(T queuedRequest)
    {
        return queuedRequest.getRequest();
    }
}
