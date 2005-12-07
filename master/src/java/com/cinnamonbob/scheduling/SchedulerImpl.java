package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public interface SchedulerImpl
{
    void schedule(Trigger trigger, Task task) throws SchedulingException;

    void unschedule(Trigger trigger) throws SchedulingException;

    void trigger(Trigger trigger, Task task) throws SchedulingException;

    void trigger(Trigger trigger, Task task, TaskExecutionContext context) throws SchedulingException;

    void pause(Trigger trigger) throws SchedulingException;

    void resume(Trigger trigger) throws SchedulingException;
}
