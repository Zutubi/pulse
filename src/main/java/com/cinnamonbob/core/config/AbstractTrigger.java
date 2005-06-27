package com.cinnamonbob.core.config;


/**
 * 
 *
 */
public abstract class AbstractTrigger implements Trigger, ProjectComponent
{
    protected Project project;
    protected Schedule schedule;
    
    public void setProject(Project project)
    {
        this.project = project;
    }

    public Project getProject()
    {
        return project;
    }
    
    public void setSchedule(Schedule schedule)
    {
        this.schedule = schedule;
    }
}
