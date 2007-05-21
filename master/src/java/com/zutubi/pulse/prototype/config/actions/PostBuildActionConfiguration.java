package com.zutubi.pulse.prototype.config.actions;

import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.config.annotations.SymbolicName;

/**
 *
 *
 */
@SymbolicName("internal.postBuildActionConfig")
public abstract class PostBuildActionConfiguration extends AbstractNamedConfiguration
{
    public abstract void execute();
}
