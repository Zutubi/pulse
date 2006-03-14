package com.cinnamonbob.scheduling;

import com.cinnamonbob.core.ObjectFactory;
import com.cinnamonbob.scheduling.persistence.TriggerDao;
import com.cinnamonbob.util.logging.Logger;

import java.util.Set;
import java.util.HashSet;

/**
 * The default trigger handler handles the details of firing a trigger.
 */
public class DefaultTriggerHandler implements TriggerHandler
{
    private static final Logger LOG = Logger.getLogger(DefaultTriggerHandler.class);

    private TriggerDao triggerDao;
    private ObjectFactory objectFactory;

    private final Set<String> executingTriggers = new HashSet<String>();

    public void fire(Trigger trigger) throws SchedulingException
    {
        TaskExecutionContext context = new TaskExecutionContext();
        fire(trigger, context);
    }

    public void fire(Trigger trigger, TaskExecutionContext context) throws SchedulingException
    {
        // do we want to execute this task?
        String triggerKey = trigger.getName() + ":" + trigger.getGroup();
        if (executingTriggers.contains(triggerKey))
        {
            LOG.info("Request to fire trigger '" + triggerKey +
                    "' ignored since the trigger is already firing.");
            return;
        }
        try
        {
            synchronized(executingTriggers)
            {
                executingTriggers.add(triggerKey);
            }

            context.setTrigger(trigger);
            trigger.fire();

            // determine the task to be executed.
            try
            {
                Task task = objectFactory.buildBean(trigger.getTaskClass());
                task.execute(context);
            }
            catch (Exception e)
            {
                // the transaction will still commit if a schedulingException is
                // thrown because it is declared in the method definition.
                throw new SchedulingException(e);
            }
        }
        finally
        {
            synchronized(executingTriggers)
            {
                executingTriggers.remove(triggerKey);
            }
        }
    }

    /**
     * Required resource.
     *
     * @param triggerDao
     */
    public void setTriggerDao(TriggerDao triggerDao)
    {
        this.triggerDao = triggerDao;
    }

    /**
     * Required resource.
     *
     * @param objectFactory
     */
    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
