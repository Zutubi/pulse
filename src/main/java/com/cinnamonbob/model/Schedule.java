package com.cinnamonbob.model;

import java.util.List;
import java.util.LinkedList;

import com.cinnamonbob.BobServer;

/**
 * Group of triggers.
 *
 */
public class Schedule extends Entity
{
    private String name;
    private Project project;
    private Trigger trigger;
    private Task task;

    public Schedule()
    {
        // noop
    }
    
    public Schedule(String name, Project project, Task task, Trigger trigger)
    {
        this.name = name;
        this.project = project;
        this.task = task;
        this.trigger = trigger;
    }
    
    public Project getProject()
    {
        return project;
    }
    
    public void setProject(Project project)
    {
        this.project = project;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     *
     * @param trigger
     */ 
    public void setTrigger(Trigger trigger)
    {
        this.trigger = trigger;
    }

    public Trigger getTrigger()
    {
        return this.trigger;
    }

    public void setTask(Task task)
    {
        this.task = task;
    }

    public Task getTask()
    {
        return this.task;
    }

    /**
     * 
     */ 
    public void triggered()
    {
        task.execute();
    }

}
