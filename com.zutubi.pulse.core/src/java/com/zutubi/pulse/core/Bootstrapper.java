package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.BuildException;

/**
 * Bootstrappers are used to initialise a working area ready for a build.
 */
public interface Bootstrapper
{
    /**
     */
    void bootstrap(PulseExecutionContext context) throws BuildException;
    void terminate();
}
