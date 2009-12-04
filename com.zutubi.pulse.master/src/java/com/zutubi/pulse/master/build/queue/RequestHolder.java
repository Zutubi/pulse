package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;

/**
 * The base class for queued and activated requests that are
 * tracked within the scheduling system.
 */
public abstract class RequestHolder
{
    private BuildRequestEvent request;

    protected RequestHolder(BuildRequestEvent request)
    {
        this.request = request;
    }

    /**
     * The id that defines the meta build this request is
     * associated with.
     *
     * @return the request meta build id.
     */
    public long getMetaBuildId()
    {
        return request.getMetaBuildId();
    }

    /**
     * Get the request instance itself.
     *
     * @return the build request event.
     */
    public BuildRequestEvent getRequest()
    {
        return request;
    }
}
