package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.scheduling.BuildProjectTask;
import com.cinnamonbob.scheduling.CronTrigger;
import com.cinnamonbob.scheduling.Schedule;

/**
 *
 *
 */
public class CreateScheduleAction extends ProjectActionSupport
{
    private long project;
    private String name;
    private String recipe;
    private String cronExpression;

    public long getProject()
    {
        return project;
    }

    public void setProject(long project)
    {
        this.project = project;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getRecipe()
    {
        return recipe;
    }

    public void setRecipe(String recipe)
    {
        this.recipe = recipe;
    }

    public String getCronExpression()
    {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        // ensure the project id is valid.
        Project project = getProjectManager().getProject(this.project);
        if (project == null)
        {
            addActionError("No project with id '" + Long.toString(this.project) + "'");
            return;
        }

        Schedule schedule = getScheduleManager().getSchedule(project, name);
        // ensure that the name is unique to the project.
        if (schedule != null)
        {
            addFieldError("name", "Name already within this project.");
        }
    }

    public String execute()
    {
        Project project = getProjectManager().getProject(getProject());

        CronTrigger trigger = new CronTrigger(cronExpression);
        BuildProjectTask task = new BuildProjectTask(project, recipe);

        getScheduleManager().schedule(name, project, trigger, task);

        return SUCCESS;
    }

    public String doDefault()
    {
        return SUCCESS;
    }
}
