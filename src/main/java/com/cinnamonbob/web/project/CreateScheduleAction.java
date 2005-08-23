package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Schedule;
import com.cinnamonbob.model.Project;

/**
 *
 *
 */
public class CreateScheduleAction extends ProjectActionSupport
{
    private long project;
    private Schedule schedule = new Schedule();

    public long getProject()
    {
        return project;
    }

    public void setProject(long project)
    {
        this.project = project;
    }
    
    public Schedule getSchedule()
    {
        return schedule;
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
        Schedule projectsSchedule = project.getSchedule(schedule.getName());
        if (projectsSchedule != null)
        {
            addFieldError("schedule.name", "Name already within this project.");
        }
    }

    public String execute()
    {
        if(schedule.getRecipe().length() == 0)
        {
            schedule.setRecipe(null);
        }
        
        Project project = getProjectManager().getProject(getProject());
        project.addSchedule(schedule);
        getProjectManager().save(project);
        return SUCCESS;
    }

    public String doDefault()
    {
        return SUCCESS;
    }

}
