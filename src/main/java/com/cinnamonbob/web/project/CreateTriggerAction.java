package com.cinnamonbob.web.project;

import java.text.ParseException;

import com.cinnamonbob.model.CronTrigger;
import com.cinnamonbob.model.Schedule;
import com.cinnamonbob.model.Project;

/**
 *
 *
 */
public class CreateTriggerAction extends ProjectActionSupport
{
    private long projectId;
    private String scheduleName;
    private Schedule schedule;
    private Project project;
    private CronTrigger trigger = new CronTrigger();
    
    public long getProjectId()
    {
        return projectId;
    }
    
    public void setProjectId(long id)
    {
        projectId = id;
    }

    public String getName()
    {
        return scheduleName;
    }
    
    public void setName(String name)
    {
        scheduleName = name;
    }
    
    public Project getProject()
    {
        return project;
    }
    
    public Schedule getSchedule()
    {
        return schedule;
    }
    
    public CronTrigger getTrigger()
    {
        return trigger;
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        // ensure the project id is valid.
        project = getProjectManager().getProject(projectId);
        if (project == null)
        {
            addActionError("No project with id '" + Long.toString(projectId) + "'");
            return;
        }

        // ensure that the name is unique to the project.
        schedule = project.getSchedule(scheduleName);
        if(schedule == null)
        {
            addActionError("No schedule with name '" + scheduleName + "' within this project.");
        }
        
        try
        {
            org.quartz.CronTrigger dummy = new org.quartz.CronTrigger("name", "group", trigger.getCronExpression());
        }
        catch(ParseException e)
        {
            addFieldError("trigger.cronExpression", e.getMessage());
        }
    }

    public String execute()
    {
        schedule.add(trigger);
        getProjectManager().save(project);
        trigger.enable();
        return SUCCESS;
    }

    public String doDefault()
    {
        return SUCCESS;
    }

}
