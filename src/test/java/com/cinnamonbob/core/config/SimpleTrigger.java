package com.cinnamonbob.core.config;

import com.cinnamonbob.core.config.BobFile;
import com.cinnamonbob.core.config.BobFileComponent;
import com.cinnamonbob.core.config.Schedule;
import com.cinnamonbob.core.config.Trigger;

/**
 * 
 *
 */
public class SimpleTrigger implements Trigger, BobFileComponent
{
    private BobFile project;
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

    public BobFile getProject()
    {
        return project;
    }

    public void setBobFile(BobFile project)
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
