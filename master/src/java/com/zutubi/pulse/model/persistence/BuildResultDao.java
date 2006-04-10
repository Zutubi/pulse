package com.zutubi.pulse.model.persistence;

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.RecipeResultNode;

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
}
