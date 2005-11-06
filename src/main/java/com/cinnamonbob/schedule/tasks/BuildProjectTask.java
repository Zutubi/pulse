package com.cinnamonbob.schedule.tasks;

import com.cinnamonbob.model.Project;

/**
 * <class-comment/>
 */
public class BuildProjectTask extends Task
{
    private String recipe;
    private Project project;

    public BuildProjectTask()
    {

    }

    public BuildProjectTask(Project project, String recipe)
    {
        this.project = project;
        this.recipe = recipe;
    }


    public String getRecipe()
    {
        return recipe;
    }

    public void setRecipe(String recipe)
    {
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

    public void execute()
    {

    }
}
