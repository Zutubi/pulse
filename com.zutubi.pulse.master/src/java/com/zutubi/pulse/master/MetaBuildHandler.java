package com.zutubi.pulse.master;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;

/**
 * The meta build handler is responsible for the coordination of one or more
 * related builds into a 'meta build'.
 */
public interface MetaBuildHandler
{
    /**
     * The unique id for the 'build' that is managed by this handler.  That is, if
     * this handler creates 3 related builds from a build request, each of these
     * builds is considered to be part of the same 'meta' build, and will have the
     * same meta build id.
     *
     * @return  unique meta build id.
     */
    Long getMetaBuildId();

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
