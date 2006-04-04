package com.cinnamonbob.scheduling;

import com.cinnamonbob.core.ObjectFactory;
import com.cinnamonbob.util.logging.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * The default trigger handler handles the details of firing a trigger.
 */
public class DefaultTriggerHandler implements TriggerHandler
{
    private static final Logger LOG = Logger.getLogger(DefaultTriggerHandler.class);

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
        synchronized(executingTriggers)
        {
            if (executingTriggers.contains(triggerKey))
            {
                LOG.info("Request to fire trigger '" + triggerKey + "' ignored since the trigger is already firing.");
                return;
            }
            LOG.info("executing trigger " + triggerKey);
            executingTriggers.add(triggerKey);
        }

        try
        {
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
                if (!executingTriggers.remove(triggerKey))
                {
                    LOG.error("failed to remove trigger key from set.");
                }
                else
                {
                    LOG.info("finished trigger " + triggerKey);
                }
            }
        }
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
