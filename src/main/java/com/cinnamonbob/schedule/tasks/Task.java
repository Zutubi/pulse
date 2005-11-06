package com.cinnamonbob.schedule.tasks;

import com.cinnamonbob.core.model.Entity;
import com.cinnamonbob.schedule.Schedule;

/**
 * <class-comment/>
 */
public abstract class Task extends Entity
{
    private String name;
    private String group;

    public void execute()
    {

    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    public void setGroup(String group)
    {
        this.group = group;
    }

    public String getGroup()
    {
        return this.group;
    }
}
