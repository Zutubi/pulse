package com.zutubi.pulse.master.scheduling;

import com.zutubi.pulse.core.Stoppable;

import java.util.List;

/**
 * The scheduler defines the scheduling services available to the system.
 */
public interface Scheduler extends Stoppable
{
    /**
     * Retrieve the trigger with the specified name that belongs to the
     * specified group.
     *
     * @param name  the name of the trigger.
     * @param group the group of the trigger.
     * @return the trigger identified by the name and group.
     */
    Trigger getTrigger(String name, String group);

    /**
     * Retrieve the trigger specified by the id.
     *
     * @param id uniquely identifying the trigger.
     * @return the trigger instance, or null if the trigger matching
     *         the id could not be found.
     */
    Trigger getTrigger(long id);

    /**
     * Retrieve the list of all the scheduled triggers.
     *
     * @return a list of scheduled triggers.
     */
    List<Trigger> getTriggers();

    /**
     * Retrieve all of the triggers associated with the specified project
     *
     * @param projectId is the identifier for the project.
     * @return a list of triggers associated with the specified project, or
     *         an empty list if no triggers are found.
     */
    List<Trigger> getTriggers(long projectId);

    /**
     * Retrieve the trigger identified by the specified name and associated
     * with the specified project.
     *
     * @param project   the id of the project the trigger belongs to.
     * @param name      the name of the trigger.
     * @return the trigger identified by the name and project id.
     */
    Trigger getTrigger(long project, String name);

    /**
     * Schedule the specified trigger.
     *
     * The combination of name and group of the new trigger must be unique and the trigger
     * state not scheduled, else an exception will be thrown.
     *
     * @param trigger   the trigger to be scheduled.
     *
     * @throws SchedulingException on error.
     */
    void schedule(Trigger trigger) throws SchedulingException;

    /**
     * Unschedule a trigger. Only scheduled triggers can be unscheduled.
     *
     * @param trigger instance to be unscheduled.
     *
     * @throws SchedulingException if there is a problem unscheduling the trigger.
     * For example, if the trigger is not scheduled.
     */
    void unschedule(Trigger trigger) throws SchedulingException;

    /**
     * Update the details of the trigger.
     *
     * @param trigger   the trigger being updated.
     *
     * @throws SchedulingException on error.
     */
    void update(Trigger trigger) throws SchedulingException;

    /**
     * Pause the triggers that belong to the specified group. A trigger
     * will not fire while it is paused.
     *
     * @param group the trigger group being paused.
     * @throws SchedulingException on error.
     */
    void pause(String group) throws SchedulingException;

    /**
     * Pause the specified trigger.  If the trigger is already paused,
     * no change is made.
     * 
     * @param trigger the trigger to be paused.
     * 
     * @throws SchedulingException on error.
     */
    void pause(Trigger trigger) throws SchedulingException;

    /**
     * Resume the triggers that belong to the specified group.
     *
     * @param group the trigger group being resumed.
     *
     * @throws SchedulingException on error.
     *
     * @see #pause(String)
     * @see #resume(Trigger)
     */
    void resume(String group) throws SchedulingException;

    /**
     * Resume the specified trigger. If the trigger is not currently
     * paused, no change is made.
     *
     * @param trigger   the trigger to be resumed.
     *
     * @throws SchedulingException on error.
     */
    void resume(Trigger trigger) throws SchedulingException;
}
