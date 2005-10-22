package com.cinnamonbob.model;

/**
 * <class-comment/>
 */
public abstract class Task extends Entity
{
    private Schedule schedule;

    public abstract void execute();

    public Schedule getSchedule()
    {
        return schedule;
    }

    public void setSchedule(Schedule schedule)
    {
        this.schedule = schedule;
    }
}
