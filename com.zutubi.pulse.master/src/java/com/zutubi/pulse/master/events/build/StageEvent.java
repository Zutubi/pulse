package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.model.Result;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;

/**
 */
public class StageEvent extends BuildEvent
{
    private RecipeResultNode stageNode;

    public StageEvent(Object source, BuildResult result, RecipeResultNode stageNode, ExecutionContext context)
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
