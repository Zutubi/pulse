package com.cinnamonbob.scheduling;

import com.cinnamonbob.scheduling.persistence.TriggerDao;

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class DefaultScheduler
{
    private List<SchedulerStrategy> strategies = new LinkedList<SchedulerStrategy>();

    private TriggerDao triggerDao;

    public void register(SchedulerStrategy strategy)
    {
        strategies.add(strategy);
    }

    public Trigger getTrigger(String name, String group)
    {
        return triggerDao.findByNameAndGroup(name, group);
    }

    public void schedule(Trigger trigger, Task task) throws SchedulingException
    {
        SchedulerStrategy impl = getStrategy(trigger);
        impl.schedule(trigger, task);
        triggerDao.save(trigger);
    }

    public void unschedule(Trigger trigger) throws SchedulingException
    {
        SchedulerStrategy impl = getStrategy(trigger);
        impl.unschedule(trigger);
        triggerDao.delete(trigger);
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
        SchedulerStrategy impl = getStrategy(trigger);
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
        SchedulerStrategy impl = getStrategy(trigger);
        impl.resume(trigger);
    }

    private SchedulerStrategy getStrategy(Trigger trigger)
    {
        for (SchedulerStrategy strategy : strategies)
        {
            if (strategy.canHandle(trigger))
            {
                return strategy;
            }
        }
        return null;
    }

    public void setTriggerDao(TriggerDao triggerDao)
    {
        this.triggerDao = triggerDao;
    }


}
