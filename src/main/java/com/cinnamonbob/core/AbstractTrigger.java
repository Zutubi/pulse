package com.cinnamonbob.core;


/**
 * 
 *
 */
public abstract class AbstractTrigger implements Trigger, BobFileComponent
{
    protected BobFile project;
    protected Schedule schedule;
    
    public void setBobFile(BobFile project)
    {
        this.project = project;
    }

    public BobFile getProject()
    {
        return project;
    }
    
    public void setSchedule(Schedule schedule)
    {
        this.schedule = schedule;
    }
}
