package com.zutubi.pulse.master;

import com.zutubi.pulse.master.events.build.AbstractBuildRequestEvent;

/**
 * The build handler factory is a simple factory interface responsible
 * for the construction of BuildHandler instances.
 */
public interface BuildHandlerFactory
{
    /**
     * Create a new BuildHandler instance that is configured to process
     * the specified build request.
     *
     * @param request the build request details.
     * @return the new build handler instance.
     */
    BuildHandler createHandler(AbstractBuildRequestEvent request);
}
