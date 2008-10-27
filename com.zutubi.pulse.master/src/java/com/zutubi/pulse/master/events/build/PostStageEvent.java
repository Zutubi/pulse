package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;

/**
 */
public class PostStageEvent extends StageEvent
{
    public PostStageEvent(Object source, BuildResult result, RecipeResultNode stageNode, PulseExecutionContext context)
    {
        super(source, result, stageNode, context);
    }
}
