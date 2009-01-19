package com.zutubi.pulse.core.engine;

import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.tove.annotations.SymbolicName;

/**
 */
@SymbolicName("zutubi.abstractCommandConfig")
public abstract class AbstractCommandConfiguration extends AbstractNamedConfiguration implements CommandConfiguration
{
    private boolean force = false;

    public boolean isForce()
    {
        return force;
    }

    public void setForce(boolean force)
    {
        this.force = force;
    }
}
