package com.cinnamonbob.model;

import java.util.List;

/**
 *
 *
 */
public interface ScheduleManager extends EntityManager<Schedule>
{
    public Schedule getSchedule(long id);

    public List<Schedule> getSchedules(Project project);
}
