package com.zutubi.pulse.master;

import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.pulse.master.model.BuildResult;

import java.io.File;

/**
 */
public interface RecipeResultCollector
{
    void prepare(BuildResult result, long recipeId);

    void collect(BuildResult result, long recipeId, boolean collectWorkingCopy, boolean incremental, AgentService agentService);

    void cleanup(BuildResult result, long recipeId, boolean incremental, AgentService agentService);

    File getRecipeDir(BuildResult result, long recipeId);
}
