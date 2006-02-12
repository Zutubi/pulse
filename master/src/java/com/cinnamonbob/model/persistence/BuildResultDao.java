package com.cinnamonbob.model.persistence;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.RecipeResult;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.RecipeResultNode;
import com.cinnamonbob.model.BuildSpecification;

import java.util.List;

public interface BuildResultDao extends EntityDao<BuildResult>
{
    void save(RecipeResultNode node);

    public void save(RecipeResult result);

    public void save(CommandResult result);

    public List<BuildResult> findLatestByProject(final Project project, final int max);

    List<BuildResult> findLatestCompleted(Project project, BuildSpecification spec, int max);

    List<BuildResult> findOldestByProject(Project project, int max);

    List<BuildResult> findOldestByProject(Project project, int first, int max);

    public BuildResult findByProjectAndNumber(final Project project, final long number);

    CommandResult findCommandResult(long id);

    RecipeResultNode findRecipeResultNode(long id);

    RecipeResult findRecipeResult(long id);

}
