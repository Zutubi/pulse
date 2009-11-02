package com.zutubi.pulse.master.build.control;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;

/**
 * The build controller factory is a simple factory interface responsible
 * for the construction of BuildController instances.
 */
public interface BuildControllerFactory
{
    /**
     * Create a new BuildController instance that is configured to process
     * the specified build request.
     *
     * @param request the build request details.
     * @return the new build controller instance.
     */
    BuildController create(BuildRequestEvent request);
}
