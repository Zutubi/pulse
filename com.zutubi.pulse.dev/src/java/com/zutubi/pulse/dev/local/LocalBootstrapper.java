package com.zutubi.pulse.dev.local;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;

/**
 * Boostrapper for local builds.
 */
public class LocalBootstrapper extends BootstrapperSupport
{
    public void bootstrap(CommandContext commandContext) throws BuildException
    {
        // Noop.  Ideally the processor would not require a bootstrapper, but
        // that comes with extra baggage not worth pursuing at this moment.
    }
}
