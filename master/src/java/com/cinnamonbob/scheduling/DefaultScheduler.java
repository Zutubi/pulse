package com.cinnamonbob.scheduling;

import com.cinnamonbob.scheduling.persistence.TriggerDao;

import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public class DefaultScheduler
{
    private Map<Class, SchedulerImpl> schedulers = new HashMap<Class, SchedulerImpl>();

    private TriggerDao triggerDao;

    public void register(Class triggerType, SchedulerImpl scheduler)
    {
        schedulers.put(triggerType, scheduler);
    }

    public Trigger getTrigger(String name, String group)
    {
        return triggerDao.findByNameAndGroup(name, group);
    }

    public void schedule(Trigger trigger, Task task) throws SchedulingException
    {
        SchedulerImpl impl = schedulers.get(trigger.getClass());
        impl.schedule(trigger, task);
        triggerDao.save(trigger);
    }

    public void unschedule(Trigger trigger) throws SchedulingException
    {
        SchedulerImpl impl = schedulers.get(trigger.getClass());
        impl.unschedule(trigger);
        triggerDao.delete(trigger);
    }

    public void trigger(Trigger trigger, Task task) throws SchedulingException
    {
        SchedulerImpl impl = schedulers.get(trigger.getClass());
        impl.trigger(trigger, task);
    }

    public void pause(String group) throws SchedulingException
    {
        for (Trigger trigger: triggerDao.findByGroup(group))
        {
            pause(trigger);
        }
    }

    public void pause(Trigger trigger) throws SchedulingException
    {
        SchedulerImpl impl = schedulers.get(trigger.getClass());
        impl.pause(trigger);
    }

    public void resume(String group) throws SchedulingException
    {
        for (Trigger trigger: triggerDao.findByGroup(group))
        {
            resume(trigger);
        }
    }

    public void resume(Trigger trigger) throws SchedulingException
    {
        SchedulerImpl impl = schedulers.get(trigger.getClass());
        impl.resume(trigger);
    }

    public void setTriggerDao(TriggerDao triggerDao)
    {
        this.triggerDao = triggerDao;
    }

    
}
