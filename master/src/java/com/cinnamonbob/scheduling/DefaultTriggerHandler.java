package com.cinnamonbob.scheduling;

import com.cinnamonbob.core.ObjectFactory;
import com.cinnamonbob.scheduling.persistence.TriggerDao;
import com.cinnamonbob.spring.SpringObjectFactory;

/**
 * <class-comment/>
 */
public class DefaultTriggerHandler implements TriggerHandler
{
    private TriggerDao triggerDao;
    private ObjectFactory objectFactory = new SpringObjectFactory();

    public void trigger(Trigger trigger) throws SchedulingException
    {
        TaskExecutionContext context = new TaskExecutionContext();
        trigger(trigger, context);
    }

    public void trigger(Trigger trigger, TaskExecutionContext context) throws SchedulingException
    {
        context.setTrigger(trigger);
        trigger.trigger();
        triggerDao.save(trigger);

        // determine the task to be executed.
        try
        {
            Task task = objectFactory.buildBean(trigger.getTaskClass());
            task.execute(context);
        }
        catch (Exception e)
        {
            throw new SchedulingException(e);
        }
    }

    public void setTriggerDao(TriggerDao triggerDao)
    {
        this.triggerDao = triggerDao;
    }
}
