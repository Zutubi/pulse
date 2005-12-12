package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public interface SchedulerStrategy
{
    /**
     * Returns true if this trigger strategy implementation knows how to deal with the
     * specified trigger.
     *
     * @param trigger
     *
     * @return true if this strategy can handle the trigger, false otherwise.
     */
    boolean canHandle(Trigger trigger);

    void schedule(Trigger trigger) throws SchedulingException;

    void unschedule(Trigger trigger) throws SchedulingException;

    void pause(Trigger trigger) throws SchedulingException;

    void resume(Trigger trigger) throws SchedulingException;

    void setTriggerHandler(TriggerHandler handler);
}
