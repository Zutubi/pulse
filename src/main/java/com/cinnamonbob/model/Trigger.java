package com.cinnamonbob.model;


/**
 * 
 *
 */
public abstract class Trigger extends Entity
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
