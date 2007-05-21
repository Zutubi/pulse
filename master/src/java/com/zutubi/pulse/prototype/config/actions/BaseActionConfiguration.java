package com.zutubi.pulse.prototype.config.actions;

import com.zutubi.pulse.core.config.AbstractNamedConfiguration;

/**
 *
 *
 */
public class BaseActionConfiguration extends AbstractNamedConfiguration
{
    private boolean failBuildOnError;


    public boolean isFailBuildOnError()
    {
        return failBuildOnError;
    }

    public void setFailBuildOnError(boolean failBuildOnError)
    {
        this.failBuildOnError = failBuildOnError;
    }
}
