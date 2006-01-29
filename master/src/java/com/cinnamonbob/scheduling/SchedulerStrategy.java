package com.cinnamonbob.scheduling;

import com.cinnamonbob.core.Stoppable;

/**
 * <class-comment/>
 */
public interface SchedulerStrategy extends Stoppable
{
    String canHandle();

    void schedule(Trigger trigger) throws SchedulingException;

    void unschedule(Trigger trigger) throws SchedulingException;

    void pause(Trigger trigger) throws SchedulingException;

    void resume(Trigger trigger) throws SchedulingException;

    void setTriggerHandler(TriggerHandler handler);
}
