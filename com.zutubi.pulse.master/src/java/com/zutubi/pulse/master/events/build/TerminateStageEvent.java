package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;

/**
 * An event raised when a request has been made to terminate a stage (either via a timeout or
 * explicit user request).
 */
public class TerminateStageEvent extends StageEvent
{
    public TerminateStageEvent(Object source, BuildResult result, RecipeResultNode stageNode, PulseExecutionContext context)
    {
        super(source, result, stageNode, context);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("Terminate Stage Event");
        if (getBuildResult() != null)
        {
            builder.append(": ").append(getBuildResult().getId());
        }
        if (getStageNode() != null)
        {
            builder.append(": ").append(getStageNode().getStageName());
        }
        return builder.toString();
    }
}
