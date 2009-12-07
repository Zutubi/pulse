package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;

import java.util.List;

/**
 * The build request handler is reponsible for configuring a build by processing
 * a build request and creating the set of QueuedRequests that define the build.
 *
 * At its simplest, a request handler generates a single QueuedRequest.  For
 * extended builds, the request handler will create multiple related QueuedRequest
 * instances.
 */
public interface BuildRequestHandler
{
    /**
     * The meta build id uniquely identifies all of the requests associated
     * with this handler.
     *
     * In particular, it can be used to identify related build requests.
     *
     * @return the unique meta build id.
     */
    long getMetaBuildId();

    /**
     * Prepare a list of QueuedRequest instances for the build request event.
     * The preparation of a request results in the creation of one or more
     * QueuedRequest instances.  In the simple case, a single queued request
     * is created for the provided request.  In a more complex case, a series
     * of QueuedRequests are created that represent a build tree.
     *
     * Once prepared, the request event will be associated with this handler via 
     * the meta build id.
     *
     * @param request   the build request event being processed.
     *
     * @return a list of QueuedRequest instances that should be enqueued with
     * the buildQueue. 
     */
    List<QueuedRequest> prepare(BuildRequestEvent request);
}