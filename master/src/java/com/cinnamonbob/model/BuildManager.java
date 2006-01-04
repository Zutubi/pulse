package com.cinnamonbob.model;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.RecipeResult;
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

    List<BuildResult> getLatestBuildResultsForProject(Project project, int max);

    BuildResult getLatestBuildResult(Project project);

    BuildResult getByProjectAndNumber(final Project project, final long number);

    CommandResult getCommandResult(long id);

    StoredArtifact getArtifact(long id);

    long getNextBuildNumber(Project project);
}
