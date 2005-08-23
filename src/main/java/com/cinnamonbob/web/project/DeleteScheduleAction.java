package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Schedule;

/**
 *
 *
 */
public class DeleteScheduleAction extends ProjectActionSupport
{
    private long id;
    private Schedule schedule;
    private long projectId;
    
    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }
    
    public Schedule getSchedule()
    {
        return schedule;
    }

    public long getProjectId()
    {
        return projectId;
    }
    
    public void validate()
    {
        schedule = getScheduleManager().getSchedule(id);
        if(schedule == null)
        {
            addActionError("Unknown schedule [" + id + "]");
        }
        
        projectId = schedule.getProject().getId();
    }

    public String execute()
    {
        getScheduleManager().delete(schedule);

        return SUCCESS;
    }
}
