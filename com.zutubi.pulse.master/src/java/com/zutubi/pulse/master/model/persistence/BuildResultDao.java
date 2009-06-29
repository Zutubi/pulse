package com.zutubi.pulse.master.model.persistence;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.model.User;

import java.util.Date;
import java.util.List;

public interface BuildResultDao extends EntityDao<BuildResult>
{
    void save(RecipeResultNode node);

    void save(RecipeResult result);

    void save(CommandResult result);

    List<BuildResult> queryBuilds(Project[] projects, ResultState[] states, long earliestStartTime, long latestStartTime, Boolean hasWorkDir, int first, int max, boolean mostRecentFirst);

    List<BuildResult> queryBuildsWithMessages(Project[] projects, Feature.Level level, int max);

    List<BuildResult> findLatestByProject(Project project, int max);

    List<BuildResult> findSinceByProject(Project project, Date since);

    List<BuildResult> findLatestByProject(Project project, int first, int max);

    List<BuildResult> findLatestByProject(Project project, ResultState[] states, int first, int max);

    List<BuildResult> findLatestCompleted(Project project, int first, int max);

    BuildResult findPreviousBuildResult(BuildResult result);

    BuildResult findPreviousBuildResultWithRevision(final BuildResult result, final ResultState[] states);

    List<BuildResult> findOldestByProject(Project project, ResultState[] states, int max, boolean includePersonal);

    BuildResult findByProjectAndNumber(final Project project, final long number);

    BuildResult findByUserAndNumber(User user, long id);

    CommandResult findCommandResult(long id);

    RecipeResultNode findRecipeResultNode(long id);

    RecipeResult findRecipeResult(long id);

    int getBuildCount(Project project, ResultState[] states);

    int getBuildCount(Project project, ResultState[] states, Boolean hasWorkDir);

    int getBuildCount(Project project, long after, long upTo);

    List<BuildResult> queryBuilds(Project project, ResultState[] states, long lowestNumber, long highestNumber, int first, int max, boolean mostRecentFirst, boolean initialise);

    List<BuildResult> findByUser(User user);

    List<BuildResult> getLatestByUser(User user, ResultState[] states, int max);

    int getCompletedResultCount(User user);

    List<BuildResult> getOldestCompletedBuilds(User user, int limit);

    List<BuildResult> getOldestBuilds(Project project, ResultState[] states, Boolean hasWorkDir, int limit);

    RecipeResultNode findResultNodeByResultId(long id);

    BuildResult findLatest();

    CommandResult findCommandResultByArtifact(long artifactId);

    BuildResult findLatestSuccessfulByProject(Project project);

    BuildResult findLatestSuccessful();
}
