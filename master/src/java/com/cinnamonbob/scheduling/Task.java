package com.cinnamonbob.scheduling;

import com.cinnamonbob.core.model.Entity;

import java.util.Map;
import java.util.HashMap;

/**
 * <class-comment/>
 */
public abstract class Task extends Entity
{
    /**
     * The name used to identify this task instance.
     */
    private String name;

    /**
     * The group to which this task instance belongs.
     */
    private String group;

    private Map dataMap;

    public Task()
    {

    }

    public Task(String name)
    {
        this(name, null);
    }

    public Task(String name, String group)
    {
        this.name = name;
        this.group = group;
    }

    public abstract void execute(TaskExecutionContext context);

    public Map getDataMap()
    {
        if (dataMap == null)
        {
            dataMap = new HashMap();
        }
        return dataMap;
    }

    private void setDataMap(Map map)
    {
        this.dataMap = map;
    }

    public String getGroup()
    {
        return group;
    }

    private void setGroup(String group)
    {
        this.group = group;
    }

    public String getName()
    {
        return name;
    }

    private void setName(String name)
    {
        this.name = name;
    }
}
