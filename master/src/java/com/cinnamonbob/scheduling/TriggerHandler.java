package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public interface TriggerHandler
{
    void fire(Trigger trigger) throws SchedulingException;

    void fire(Trigger trigger, TaskExecutionContext context) throws SchedulingException;
}
