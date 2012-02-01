package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;

/**
 * An event raised just after a build stage is assigned to an agent, before it
 * is dispatched.
 */
public class PreStageEvent extends StageEvent
{
    public PreStageEvent(Object source, BuildResult result, RecipeResultNode stageNode, PulseExecutionContext context)
    {
        super(source, result, stageNode, context);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("Pre Stage Event");
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
