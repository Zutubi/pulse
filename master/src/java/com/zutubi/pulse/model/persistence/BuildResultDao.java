package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.RecipeResultNode;
import com.zutubi.pulse.model.User;

import java.util.List;

public interface BuildResultDao extends EntityDao<BuildResult>
{
    void save(RecipeResultNode node);

    void save(RecipeResult result);

    void save(CommandResult result);

    List<BuildResult> queryBuilds(Project[] projects, ResultState[] states, String[] specs, long earliestStartTime, long latestStartTime, Boolean hasWorkDir, int first, int max, boolean mostRecentFirst);

    List<BuildResult> findLatestByProject(Project project, int max);

    List<BuildResult> findLatestByProject(Project project, int first, int max);

    List<BuildResult> findLatestByProject(Project project, ResultState[] states, String spec, int first, int max);

    List<BuildResult> findLatestCompleted(Project project, String spec, int max);

    BuildResult findPreviousBuildResult(BuildResult result);

    List<BuildResult> findOldestByProject(Project project, int max);

    List<BuildResult> findOldestByProject(Project project, int first, int max);

    BuildResult findByProjectAndNumber(final Project project, final long number);

    CommandResult findCommandResult(long id);

    RecipeResultNode findRecipeResultNode(long id);

    RecipeResult findRecipeResult(long id);

    int getBuildCount(Project project, ResultState[] states, String spec);

    int getBuildCount(Project project, ResultState[] states, Boolean hasWorkDir);

    List<String> findAllSpecifications(Project project);

    List<String> findAllSpecificationsForProjects(Project[] projects);

    List<BuildResult> querySpecificationBuilds(Project project, String spec, ResultState[] states, long lowestNumber, long highestNumber, int first, int max, boolean mostRecentFirst, boolean initialise);

    List<BuildResult> findByUser(User user);

    List<BuildResult> getLatestBuildResultsForUser(User user, int max);

    int getCompletedResultCount(User user);

    List<BuildResult> getOldestCompletedBuilds(User user, int max);

}
