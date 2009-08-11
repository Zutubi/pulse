package com.zutubi.pulse.dev.local;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.model.CommandResult;

/**
 * Noop bootstrapper for local builds.
 */
public class LocalBootstrapper extends BootstrapperSupport
{
    public void bootstrap(PulseExecutionContext context, CommandResult result) throws BuildException
    {
        // Noop
    }
}
