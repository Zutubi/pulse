package com.zutubi.pulse.master.scheduling;

import com.zutubi.pulse.core.Stoppable;

import java.util.List;

/**
 * <class-comment/>
 */
public interface SchedulerStrategy extends Stoppable
{
    List<String> canHandle();

    void init(Trigger trigger) throws SchedulingException;

    void schedule(Trigger trigger) throws SchedulingException;

    void unschedule(Trigger trigger) throws SchedulingException;

    void pause(Trigger trigger) throws SchedulingException;

    void resume(Trigger trigger) throws SchedulingException;

    void setTriggerHandler(TriggerHandler handler);
}
