package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.commands.api.CommandContext;

/**
 * Bootstrappers are used to initialise a working area ready for a build.
 */
public interface Bootstrapper
{
    /**
     */
    void bootstrap(CommandContext commandContext) throws BuildException;
    void terminate();
}
