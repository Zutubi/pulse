package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public interface TriggerHandler
{
    void trigger(Trigger trigger) throws SchedulingException;

    void trigger(Trigger trigger, TaskExecutionContext context) throws SchedulingException;
}
