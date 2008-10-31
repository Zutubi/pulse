package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.model.Result;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;

/**
 */
public class StageEvent extends BuildEvent
{
    private RecipeResultNode stageNode;

    public StageEvent(Object source, BuildResult result, RecipeResultNode stageNode, PulseExecutionContext context)
    {
        super(source, result, context);
        this.stageNode = stageNode;
    }

    public RecipeResultNode getStageNode()
    {
        return stageNode;
    }

    public Result getResult()
    {
        return stageNode.getResult();
    }
}
