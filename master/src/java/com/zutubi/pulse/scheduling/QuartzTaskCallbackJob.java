package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.util.logging.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * <class-comment/>
 */
public class QuartzTaskCallbackJob implements Job
{
    private static final Logger LOG = Logger.getLogger(QuartzTaskCallbackJob.class);

    public static final String TRIGGER_PROP = "trigger";

    private TriggerHandler triggerHandler;

    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        LOG.entering();

        // notify schedule manager that this trigger has been activated.
        JobDataMap data = context.getMergedJobDataMap();
        try
        {
            Trigger trigger = (Trigger) data.get(TRIGGER_PROP);
            triggerHandler.fire(trigger);
        }
        catch (SchedulingException e)
        {
            throw new JobExecutionException(e);
        }

        LOG.exiting();
    }

    public void setTriggerHandler(TriggerHandler triggerHandler)
    {
        this.triggerHandler = triggerHandler;
    }
}
