package com.zutubi.pulse.events.build;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;
import com.zutubi.pulse.core.model.Result;

/**
 */
public class StageEvent extends BuildEvent
{
    private RecipeResultNode stageNode;

    public StageEvent(Object source, BuildResult result, RecipeResultNode stageNode)
    {
        super(source, result);
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
