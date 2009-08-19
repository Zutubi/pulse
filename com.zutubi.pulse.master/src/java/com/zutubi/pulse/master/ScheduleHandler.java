package com.zutubi.pulse.master;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;

/**
 * The schedule handler is responsible for the scheduling of a build with the
 * build queue.  It is used by the FatController to allow a build request to
 * span multiple builds.
 */
public interface ScheduleHandler
{
    /**
     * The unique id for the 'build' that is managed by this handler.  That is, if
     * this scheduler creates 3 related builds from a build request, each of these
     * builds is considered to be part of the same 'meta' build, and will have the
     * same meta build id.
     *
     * @return  unique build id.
     */
    Long getMetaBuildId();

    /**
     * If true, this handler manages more than one build and will therefore receive
     * all build request events that match its build id.
     *
     * @return true if this handler receives multiple requests, false otherwise.
     */
    boolean isMultiBuild();

    /**
     * This method is where all the work is done.  The handler processes the build request,
     * and, if appropriate, will forward it or other requests on to the com.zutubi.pulse.master.FatController
     * for queueing.
     *
     * @param request   the request to be processed.
     */
    void handle(BuildRequestEvent request);

    /**
     * A callback to handle initialisation processing after this handler instance has been
     * constructed and wired with the appropriate resources.
     */
    void init();
}
