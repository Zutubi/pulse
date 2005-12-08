package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public interface TriggerHandler
{
    void trigger(Trigger trigger, Task task) throws SchedulingException;

    void trigger(Trigger trigger, Task task, TaskExecutionContext context) throws SchedulingException;
}
