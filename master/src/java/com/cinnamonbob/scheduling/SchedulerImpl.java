package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public interface SchedulerImpl
{
    void schedule(Trigger trigger, Task task);

    void unschedule(Trigger trigger);

    void trigger(Trigger trigger, Task task);

    void trigger(Trigger trigger, Task task, TaskExecutionContext context);

    void pause(Trigger trigger);

    void resume(Trigger trigger);
}
