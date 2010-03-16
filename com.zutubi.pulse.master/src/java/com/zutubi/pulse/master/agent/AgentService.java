package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.servercore.AgentRecipeDetails;
import com.zutubi.pulse.servercore.agent.SynchronisationMessage;
import com.zutubi.pulse.servercore.agent.SynchronisationMessageResult;

import java.io.File;
import java.util.List;

/**
 * Service for communication with agents.
 */
public interface AgentService
{
    /**
     * Returns the configuration of the agent underlying this service.
     *
     * @return the configuration of our agent
     */
    AgentConfiguration getAgentConfig();

    boolean build(RecipeRequest request);

    /**
     * Collects files produced by the recipe execution to the given local
     * directories.  Note that working copy collection is optional.
     *
     * @param recipeDetails details of the recipe used to find the results
     * @param outputDest    local directory to receive the output files
     *                      (artifacts)
     * @param workDest      local directory to receive the working copy, or null
     *                      if the working copy should not be collected
     */
    void collectResults(AgentRecipeDetails recipeDetails, File outputDest, File workDest);

    void cleanup(AgentRecipeDetails recipeDetails);

    /**
     * Terminates the given recipe if it is still running.  This method may
     * only be called *after* receiving the recipe commenced event for the
     * recipe.
     *
     * @param recipeId the recipe to terminate
     */
    void terminateRecipe(long recipeId);

    /**
     * Synchronises the agent by processing all of the given messages.
     * Messages are converted to tasks, the tasks executed and the results
     * returned.
     *
     * @param messages messages to process
     * @return results corresponding results for each of the messages
     */
    List<SynchronisationMessageResult> synchronise(List<SynchronisationMessage> messages);
}
