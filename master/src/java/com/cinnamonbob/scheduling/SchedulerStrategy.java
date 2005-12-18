package com.cinnamonbob.scheduling;

import java.util.List;

/**
 * <class-comment/>
 */
public interface SchedulerStrategy
{
    String canHandle();

    void schedule(Trigger trigger) throws SchedulingException;

    void unschedule(Trigger trigger) throws SchedulingException;

    void pause(Trigger trigger) throws SchedulingException;

    void resume(Trigger trigger) throws SchedulingException;

    void setTriggerHandler(TriggerHandler handler);
}
