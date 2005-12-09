package com.cinnamonbob.scheduling;

import com.cinnamonbob.scheduling.persistence.TaskDao;

/**
 * <class-comment/>
 */
public class DefaultTriggerHandler implements TriggerHandler
{
    private TaskDao taskDao = null;

    public void trigger(Trigger trigger) throws SchedulingException
    {
        TaskExecutionContext context = new TaskExecutionContext();
        trigger(trigger, context);
    }

    public void trigger(Trigger trigger, TaskExecutionContext context) throws SchedulingException
    {
        context.setTrigger(trigger);
        trigger.trigger();
        // determine the task to be executed.
        Task task = taskDao.findByNameAndGroup(trigger.getTaskName(), trigger.getTaskGroup());
        task.execute(context);
    }

    public void setTaskDao(TaskDao dao)
    {
        this.taskDao = dao;
    }
}
