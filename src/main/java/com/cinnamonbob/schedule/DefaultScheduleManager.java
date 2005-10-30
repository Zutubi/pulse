package com.cinnamonbob.schedule;

import com.cinnamonbob.schedule.persistence.ScheduleDao;
import com.cinnamonbob.bootstrap.ComponentContext;

import static com.cinnamonbob.schedule.QuartzSupport.*;
import com.cinnamonbob.model.Project;

import java.util.List;

import org.quartz.Scheduler;
import org.quartz.JobDetail;

/**
 * <class-comment/>
 */
public class DefaultScheduleManager implements ScheduleManager
{
    private ScheduleDao scheduleDao;
    private Scheduler scheduler;

    public void setScheduleDao(ScheduleDao scheduleDao)
    {
        this.scheduleDao = scheduleDao;
    }

    public void setQuartzScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    // when we run this initialisation, we need to ensure that the spring context is setup
    // so that the triggers have the available resources. We need a post setup event.

    public void init() throws Exception
    {
        // initialise the quartz callback job.
        JobDetail detail = new JobDetail(WRAPPER_NAME, WRAPPER_GROUP, QuartzTaskWrapper.class);
        scheduler.addJob(detail, true);

        // enable self.
        List<Trigger> triggers = scheduleDao.findAllTriggers();
        for (Trigger trigger : triggers)
        {
            // need to handle some form of autowire... unfortunately, context many not be completely
            // setup at this point since its part of the initialisation...
            ((QuartzCronTrigger)trigger).setQuartzScheduler(scheduler);
            trigger.enable();
        }
    }

    protected void triggerActivated(long id)
    {
        // lookup schedule by trigger.
        Schedule schedule = scheduleDao.findByTrigger(id);
        schedule.getTask().execute();
    }

    public Schedule getSchedule(long id)
    {
        return scheduleDao.findById(id);
    }

    public Schedule getSchedule(Project project, String name)
    {
        return scheduleDao.findBy(project, name);
    }

    public void delete(Schedule schedule)
    {
        scheduleDao.delete(schedule);
    }

    public void schedule(String name, Project project, Trigger trigger, Task task) throws SchedulingException
    {
        Schedule schedule = new Schedule(project, name, trigger, task);
        ComponentContext.autowire(trigger);
        scheduleDao.save(schedule);
        trigger.enable();
    }

    public List<Schedule> getSchedules(Project project)
    {
        return scheduleDao.findByProject(project);
    }

}
