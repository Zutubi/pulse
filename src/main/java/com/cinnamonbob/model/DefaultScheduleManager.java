package com.cinnamonbob.model;

import com.cinnamonbob.model.persistence.ScheduleDao;

import java.util.List;

/**
 *
 *
 */
public class DefaultScheduleManager implements ScheduleManager
{
    private ScheduleDao scheduleDao;

    public void setScheduleDao(ScheduleDao dao)
    {
        this.scheduleDao = dao;
    }

    public void save(Schedule schedule)
    {
        scheduleDao.save(schedule);
    }

    public void delete(Schedule schedule)
    {
        // un do associations.
        schedule.getProject().remove(schedule);
        scheduleDao.delete(schedule);
    }

    public Schedule getSchedule(long id)
    {
        return scheduleDao.findById(id);
    }

    public List<Schedule> getSchedules(Project project)
    {
        return scheduleDao.findByProject(project);
    }
}
