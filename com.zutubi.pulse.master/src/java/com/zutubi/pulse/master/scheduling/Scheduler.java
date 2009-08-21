package com.zutubi.pulse.master.scheduling;

import com.zutubi.pulse.core.Stoppable;

import java.util.List;

/**
 * The scheduler defines the scheduling services available to the system.
 */
public interface Scheduler extends Stoppable
{
    Trigger getTrigger(String name, String group);

    /**
     * Retrieve the trigger specified by the id.
     *
     * @param id uniquely identifying the trigger.
     * @return the trigger instance, or null if the trigger matching
     *         the id could not be found.
     */
    Trigger getTrigger(long id);

    List<Trigger> getTriggers();

    /**
     * Retrieve all of the triggers associated with the specified project
     *
     * @param projectId is the identifier for the project.
     * @return a list of triggers associated with the specified project, or
     *         an empty list if no triggers are found.
     */
    List<Trigger> getTriggers(long projectId);

    Trigger getTrigger(long project, String name);

    void schedule(Trigger trigger) throws SchedulingException;

    void trigger(Trigger trigger) throws SchedulingException;

    void unschedule(Trigger trigger) throws SchedulingException;

    void update(Trigger trigger) throws SchedulingException;

    void pause(String group) throws SchedulingException;

    void pause(Trigger trigger) throws SchedulingException;

    void resume(String group) throws SchedulingException;

    void resume(Trigger trigger) throws SchedulingException;
}
