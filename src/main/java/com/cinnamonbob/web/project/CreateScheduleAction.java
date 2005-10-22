package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Schedule;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.CronTrigger;
import com.cinnamonbob.model.BuildTask;

/**
 *
 *
 */
public class CreateScheduleAction extends ProjectActionSupport
{
    private long project;

    private String name;
    private String recipe;
    private String cron;

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
        return this.recipe;
    }

    public void setRecipe(String recipe)
    {
        this.recipe = recipe;
    }

    public String getCron()
    {
        return this.cron;
    }

    public void setCron(String cron)
    {
        this.cron = cron;
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

        // ensure that the name is unique to the project.
        Schedule projectsSchedule = project.getSchedule(getName());
        if (projectsSchedule != null)
        {
            addFieldError("name", "Name already in use within this project.");
        }
    }

    public String execute()
    {
        Project project = getProjectManager().getProject(getProject());

        BuildTask task = new BuildTask(project, recipe);
        CronTrigger trigger = new CronTrigger(cron);
        Schedule schedule = new Schedule(name, project, task, trigger);
        project.addSchedule(schedule);

        getProjectManager().save(project);
        return SUCCESS;
    }

    public String doDefault()
    {
        return SUCCESS;
    }

}
