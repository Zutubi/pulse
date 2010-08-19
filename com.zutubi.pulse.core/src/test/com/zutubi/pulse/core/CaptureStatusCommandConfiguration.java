package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * Configuration for a test command that captures the recipe status in the
 * command context. 
 */
@SymbolicName("zutubi.captureStatusCommandConfig")
public class CaptureStatusCommandConfiguration extends CommandConfigurationSupport
{
    private boolean fail = false;
    
    public CaptureStatusCommandConfiguration()
    {
        super(CaptureStatusCommand.class);
    }

    public boolean isFail()
    {
        return fail;
    }

    public void setFail(boolean fail)
    {
        this.fail = fail;
    }
}