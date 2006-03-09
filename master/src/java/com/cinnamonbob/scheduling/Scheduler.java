package com.cinnamonbob.scheduling;

import com.cinnamonbob.core.Stoppable;

import java.util.List;

/**
 * <class-comment/>
 */
public interface Scheduler extends Stoppable
{
    Trigger getTrigger(String name, String group);

    Trigger getTrigger(long id);

    List<Trigger> getTriggers();

    List<Trigger> getTriggers(long id);

    Trigger getTrigger(long project, String name);

    void schedule(Trigger trigger) throws SchedulingException;

    void trigger(Trigger trigger) throws SchedulingException;

    void unschedule(Trigger trigger) throws SchedulingException;

    void pause(String group) throws SchedulingException;

    void pause(Trigger trigger) throws SchedulingException;

    void resume(String group) throws SchedulingException;

    void resume(Trigger trigger) throws SchedulingException;

}
