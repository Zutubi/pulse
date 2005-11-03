package com.cinnamonbob.schedule;

import com.cinnamonbob.core.model.Entity;
import com.cinnamonbob.model.Project;

/**
 * This is an immutable object.
 */
public class Schedule extends Entity
{
    /**
     * The project to which this schedule is associated.
     */
    private Project project;

    /**
     * A name used to identify this schedule. This name is unique within the context
     * of a single project.
     */
    private String name;

    /**
     * The trigger that defines when to take action.
     */
    private Trigger trigger;

    /**
     * The task that defines the action to be taken.
     */
    private Task task;

    /**
     * Used by hibernate
     */
    private Schedule()
    {

    }

    public Schedule(Project project, String name, Trigger trigger, Task task)
    {
        this.project = project;
        this.name = name;
        this.trigger = trigger;
        this.task = task;
    }

    public Schedule(String name, Trigger trigger, Task task)
    {
        this(null, name, trigger, task);
    }

    public Project getProject()
    {
        return project;
    }

    /**
     * Used by hibernate
     */
    public void setProject(Project project)
    {
        this.project = project;
    }

    public String getName()
    {
        return name;
    }

    /**
     * Used by hibernate
     */
    private void setName(String name)
    {
        this.name = name;
    }

    public Trigger getTrigger()
    {
        return trigger;
    }

    /**
     * Used by hibernate
     */
    private void setTrigger(Trigger trigger)
    {
        this.trigger = trigger;
    }

    public Task getTask()
    {
        return task;
    }

    /**
     * Used by hibernate
     */
    private void setTask(Task task)
    {
        this.task = task;
    }
}
