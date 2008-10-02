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

    /**
     * Returns true iff the trigger depends on the given project.  This does not
     * include triggers for the project itself: just other triggers that are
     * somehow associated to the project (e.g. build completed triggers).
     *
     * @param trigger   the trigger to test
     * @param projectId the project to test for
     * @return true iff the given trigger depends on the given project
     */
    boolean dependsOnProject(Trigger trigger, long projectId);
}
