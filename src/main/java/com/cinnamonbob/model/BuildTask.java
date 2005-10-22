package com.cinnamonbob.model;

import com.cinnamonbob.model.Task;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.BobServer;

/**
 * <class-comment/>
 */
public class BuildTask extends Task
{
    private Project project;
    private String recipe;

    public BuildTask()
    {

    }

    public BuildTask(Project project, String recipe)
    {
        this.project = project;
        this.recipe = recipe;
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }

    public String getRecipe()
    {
        return recipe;
    }

    public void setRecipe(String recipe)
    {
        this.recipe = recipe;
    }

    public void execute()
    {
        BobServer.build(project.getName(), recipe);
    }
}
