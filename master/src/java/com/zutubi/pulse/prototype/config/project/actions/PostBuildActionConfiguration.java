package com.zutubi.pulse.prototype.config.project.actions;

import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.config.annotations.SymbolicName;

/**
 *
 *
 */
@SymbolicName("zutubi.postBuildActionConfig")
public abstract class PostBuildActionConfiguration extends AbstractNamedConfiguration
{
    public abstract void execute();
}
