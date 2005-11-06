package com.cinnamonbob.schedule;

import com.cinnamonbob.bootstrap.ComponentContext;
import com.cinnamonbob.core.event.EventManager;
import com.cinnamonbob.model.Project;
import static com.cinnamonbob.schedule.QuartzSupport.WRAPPER_GROUP;
import static com.cinnamonbob.schedule.QuartzSupport.WRAPPER_NAME;
import com.cinnamonbob.schedule.persistence.ScheduleDao;
import com.cinnamonbob.schedule.tasks.Task;
import com.cinnamonbob.schedule.triggers.CronTrigger;
import com.cinnamonbob.schedule.triggers.EventTrigger;
import com.cinnamonbob.schedule.triggers.QuartzTaskWrapper;
import com.cinnamonbob.schedule.triggers.Trigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultScheduleManager implements ScheduleManager
{
    private ScheduleDao scheduleDao;
    private Scheduler scheduler;
    private EventManager eventManager;

    public void setScheduleDao(ScheduleDao scheduleDao)
    {
        this.scheduleDao = scheduleDao;
    }

    public void setQuartzScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    // when we run this initialisation, we need to ensure that the spring context is setup
    // so that the triggers have the available resources. We need a post setup event.

    public void init() throws Exception
    {
        // initialise the quartz callback job.
        JobDetail detail = new JobDetail(WRAPPER_NAME, WRAPPER_GROUP, QuartzTaskWrapper.class);
        scheduler.addJob(detail, true);

        // synchronize state of triggers, activating those that need it.
        List<Trigger> triggers = scheduleDao.findAllTriggers();
        for (Trigger trigger : triggers)
        {
            // need to handle some form of autowire... unfortunately, context many not be completely
            // setup at this point since its part of the initialisation...
            if (trigger instanceof CronTrigger)
            {
                ((CronTrigger) trigger).setQuartzScheduler(scheduler);
            }
            else if (trigger instanceof EventTrigger)
            {
                ((EventTrigger) trigger).setEventManager(eventManager);
            }
            trigger.rehydrate();
        }
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
        trigger.activate();
    }

    public List<Schedule> getSchedules(Project project)
    {
        return scheduleDao.findByProject(project);
    }

    public Trigger getTrigger(long id)
    {
        return scheduleDao.findTriggerById(id);
    }

    public void activate(long triggerId)
    {

    }
}
