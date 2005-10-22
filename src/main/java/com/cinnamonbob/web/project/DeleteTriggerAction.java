package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Schedule;

/**
 *
 *
 */
public class DeleteTriggerAction extends ProjectActionSupport
{
    private long scheduleId;
    private long id;
    private Schedule schedule;
    
    public long getScheduleId()
    {
        return id;
    }

    public void setScheduleId(long id)
    {
        this.scheduleId = id;
    }

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

    public void validate()
    {
        schedule = getScheduleManager().getSchedule(scheduleId);
        if(schedule == null)
        {
            addActionError("Unknown schedule [" + id + "]");
        }
    }

    public String execute()
    {
        schedule.remove(id);
        getScheduleManager().save(schedule);

        return SUCCESS;
    }
}
