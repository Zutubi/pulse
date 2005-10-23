package com.cinnamonbob.scheduling;

import com.cinnamonbob.model.Entity;
import com.cinnamonbob.model.Project;

/**
 * <class-comment/>
 */
public class Schedule extends Entity
{
    private String name;
    private Project project;
    private Trigger trigger;
    private Task task;

    public Trigger getTrigger()
    {
        return trigger;
    }

    public void setTrigger(Trigger trigger)
    {
        this.trigger = trigger;
    }

    public Task getTask()
    {
        return task;
    }

    public void setTask(Task task)
    {
        this.task = task;
    }


    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }
}
