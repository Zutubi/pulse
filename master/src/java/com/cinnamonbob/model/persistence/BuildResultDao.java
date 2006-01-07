package com.cinnamonbob.model.persistence;

import com.cinnamonbob.core.model.CommandResult;
import com.cinnamonbob.core.model.RecipeResult;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.RecipeResultNode;

import java.util.List;

public interface BuildResultDao extends EntityDao<BuildResult>
{
    void save(RecipeResultNode node);

    public void save(RecipeResult result);

    public void save(CommandResult result);

    public List<BuildResult> findLatestByProject(final Project project, final int max);

    public BuildResult findByProjectAndNumber(final Project project, final long number);

    CommandResult findCommandResult(long id);

    RecipeResultNode findRecipeResultNode(long id);

    RecipeResult findRecipeResult(long id);

}
