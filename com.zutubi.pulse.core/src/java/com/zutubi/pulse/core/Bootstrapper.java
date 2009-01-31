package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.model.CommandResult;

/**
 * Bootstrappers are used to initialise a working area ready for a build.
 */
public interface Bootstrapper
{
    /**
     */
    void bootstrap(PulseExecutionContext context, CommandResult result) throws BuildException;
    void terminate();
}
