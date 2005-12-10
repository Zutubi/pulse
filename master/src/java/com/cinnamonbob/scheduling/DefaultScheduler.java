package com.cinnamonbob.scheduling;

import com.cinnamonbob.scheduling.persistence.TriggerDao;
import com.cinnamonbob.scheduling.persistence.TaskDao;

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class DefaultScheduler
{
    private List<SchedulerStrategy> strategies = new LinkedList<SchedulerStrategy>();

    private TriggerDao triggerDao;
    private TaskDao taskDao;

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
        if (triggerDao.findByNameAndGroup(trigger.getName(), trigger.getGroup()) != null)
        {
            throw new SchedulingException("A trigger with name " + trigger.getName() + " and group " + trigger.getGroup() + " has already been registered.");
        }
        if (taskDao.findByNameAndGroup(task.getName(), task.getGroup()) != null)
        {
            throw new SchedulingException("A task with name " + task.getName() + " and group " + task.getGroup() + " has already been registered.");
        }

        SchedulerStrategy impl = getStrategy(trigger);
        impl.schedule(trigger, task);

        // assosiate trigger and task so that task can be retrieved when trigger fires.
        trigger.setTaskName(task.getName());
        trigger.setTaskGroup(task.getGroup());
        triggerDao.save(trigger);
        taskDao.save(task);
    }

    public void schedule(Trigger trigger)
    {
        // todo...
    }

    public void unschedule(Trigger trigger) throws SchedulingException
    {
        SchedulerStrategy impl = getStrategy(trigger);
        impl.unschedule(trigger);
        Task task = taskDao.findByNameAndGroup(trigger.getTaskName(), trigger.getTaskGroup());
        triggerDao.delete(trigger);
        taskDao.delete(task);
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


    public void setTaskDao(TaskDao taskDao)
    {
        this.taskDao = taskDao;
    }
}
