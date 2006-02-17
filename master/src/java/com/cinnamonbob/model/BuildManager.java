package com.cinnamonbob.model;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.RecipeResult;
import com.cinnamonbob.core.model.ResultState;
import com.cinnamonbob.core.model.StoredArtifact;

import java.util.List;

/**
 * 
 *
 */
public interface BuildManager
{
    void save(BuildResult result);

    void save(RecipeResultNode node);

    void save(RecipeResult result);

    BuildResult getBuildResult(long id);

    RecipeResultNode getRecipeResultNode(long id);

    RecipeResult getRecipeResult(long id);

    CommandResult getCommandResult(long id);

    StoredArtifact getArtifact(long id);

    List<BuildResult> getLatestBuildResultsForProject(Project project, int max);

    /**
     * Fills out the list in the given history page based on the page offset.
     * Results in the initial state are NOT included.
     *
     * @param page the page to fill in
     */
    void fillHistoryPage(HistoryPage page);

    /**
     * Fills out the list in the given history page based on the given
     * criteria and the offset of the page.
     *
     * @param page   the page to fill in
     * @param states if not null, restrict to results in one of these states
     * @param spec   if no null, restrict to results of the given spec
     */
    void fillHistoryPage(HistoryPage page, ResultState[] states, BuildSpecification spec);

    List<BuildResult> getLatestCompletedBuildResults(Project project, BuildSpecification spec, int max);

    BuildResult getLatestBuildResult(Project project);

    BuildResult getByProjectAndNumber(final Project project, final long number);

    long getNextBuildNumber(Project project);

    void cleanupBuilds();
}
