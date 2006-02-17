package com.cinnamonbob.model.persistence;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.RecipeResult;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.BuildSpecification;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.RecipeResultNode;

import java.util.List;

public interface BuildResultDao extends EntityDao<BuildResult>
{
    void save(RecipeResultNode node);

    void save(RecipeResult result);

    void save(CommandResult result);

    List<BuildResult> findLatestByProject(final Project project, final int max);

    List<BuildResult> findLatestByProject(Project project, int first, int max);

    List<BuildResult> findLatestCompleted(Project project, BuildSpecification spec, int max);

    List<BuildResult> findOldestByProject(Project project, int max);

    List<BuildResult> findOldestByProject(Project project, int first, int max);

    BuildResult findByProjectAndNumber(final Project project, final long number);

    CommandResult findCommandResult(long id);

    RecipeResultNode findRecipeResultNode(long id);

    RecipeResult findRecipeResult(long id);

    int getBuildCount(Project project);
}
