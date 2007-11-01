package com.zutubi.pulse.events.build;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;
import com.zutubi.pulse.core.model.Result;

/**
 */
public class PostStageEvent extends StageEvent
{
    public PostStageEvent(Object source, BuildResult result, RecipeResultNode stageNode)
    {
        super(source, result, stageNode);
    }
}
