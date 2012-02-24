package com.zutubi.pulse.master.build.control;

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.pulse.master.model.BuildResult;

import java.io.File;

/**
 */
public interface RecipeResultCollector
{
    void prepare(BuildResult result, long recipeId);

    void collect(BuildResult result, long recipeId, ExecutionContext context, AgentService agentService);

    void cleanup(long recipeId, ExecutionContext context, AgentService agentService);

    File getRecipeDir(BuildResult result, long recipeId);
}
