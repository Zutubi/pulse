package com.cinnamonbob.scheduling;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.scheduling.persistence.ScheduleDao;

import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultScheduleManager implements ScheduleManager
{
    private ScheduleDao scheduleDao;

    public Schedule getSchedule(long id)
    {
        return scheduleDao.findById(id);
    }

    public Schedule getSchedule(Project project, String name)
    {
        return scheduleDao.findBy(project, name);
    }

    public void schedule(String name, Project project, Trigger trigger, Task task)
    {
        Schedule schedule = new Schedule();
        schedule.setTask(task);
        schedule.setTrigger(trigger);
        schedule.setProject(project);
        schedule.setName(name);

        scheduleDao.save(schedule);
    }

    public List<Schedule> getSchedules(Project project)
    {
        return scheduleDao.findByProject(project);
    }

    public void delete(Schedule schedule)
    {
        scheduleDao.delete(schedule);
    }

    public void setScheduleDao(ScheduleDao scheduleDao)
    {
        this.scheduleDao = scheduleDao;
    }
}
