package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.commands.api.CommandSupport;

import static com.zutubi.pulse.core.engine.api.BuildProperties.PROPERTY_RECIPE_STATUS;

/**
 * Simple testing command captures the recipe status from the execution context
 * to the properties stored on the command result.
 */
public class CaptureStatusCommand extends CommandSupport
{
    public static final String FAILURE_MESSAGE = "configured to fail";

    public CaptureStatusCommand(CaptureStatusCommandConfiguration config)
    {
        super(config);
    }

    public void execute(CommandContext commandContext)
    {
        commandContext.addCommandProperty(PROPERTY_RECIPE_STATUS, commandContext.getExecutionContext().getString(PROPERTY_RECIPE_STATUS));
        
        if (((CaptureStatusCommandConfiguration) getConfig()).isFail())
        {
            commandContext.failure(FAILURE_MESSAGE);
        }
    }
}
