package com.cinnamonbob.schedule;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.schedule.triggers.Trigger;
import com.cinnamonbob.schedule.tasks.Task;

import java.util.List;

/**
 * <class-comment/>
 */
public interface ScheduleManager
{
    Schedule getSchedule(long id);

    Schedule getSchedule(Project project, String name);

    void schedule(String name, Project project, Trigger trigger, Task task) throws SchedulingException;

    List<Schedule> getSchedules(Project project);

    void delete(Schedule schedule);

    void activate(long triggerId);
}
