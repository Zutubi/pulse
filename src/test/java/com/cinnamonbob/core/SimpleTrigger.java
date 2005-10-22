package com.cinnamonbob.core;

import com.cinnamonbob.core.BobFile;
import com.cinnamonbob.core.BobFileComponent;
import com.cinnamonbob.model.Schedule;
import com.cinnamonbob.model.Trigger;

/**
 * 
 *
 */
public class SimpleTrigger implements BobFileComponent
{
    private BobFile project;
    private String name;
    private String value;

    public long getId()
    {
        return 0;
    }
    
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
    
    public String getType()
    {
        return "simple";
    }
    
    public String getSummary()
    {
        return "real simple";
    }
}
