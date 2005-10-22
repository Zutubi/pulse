package com.cinnamonbob.model;


/**
 * 
 *
 */
public abstract class AbstractTrigger extends Entity implements Trigger
{
    protected Schedule schedule;
    
    public Schedule getSchedule()
    {
        return schedule;
    }
    
    public void setSchedule(Schedule schedule)
    {
        this.schedule = schedule;
    }
}
