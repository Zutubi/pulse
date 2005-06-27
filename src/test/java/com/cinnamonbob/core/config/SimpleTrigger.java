package com.cinnamonbob.core.config;

/**
 * 
 *
 */
public class SimpleTrigger implements Trigger, ProjectComponent
{
    private Project project;
    private String name;
    private String value;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }

    public void setSchedule(Schedule schedule)
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void trigger()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void enable()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void disable()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isEnabled()
    {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
