package com.cinnamonbob.scheduling;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobDataMap;

import com.cinnamonbob.util.logging.Logger;

/**
 * <class-comment/>
 */
public class QuartzTaskCallbackJob implements Job
{
    private static final Logger LOG = Logger.getLogger(QuartzTaskCallbackJob.class.getName());

    public static final String TRIGGER_PROP = "trigger";

    private TriggerHandler triggerHandler;

    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        // notify schedule manager that this trigger has been activated.
        JobDataMap data = context.getJobDetail().getJobDataMap();
        try
        {
            triggerHandler.trigger((Trigger)data.get(TRIGGER_PROP));
        }
        catch (SchedulingException e)
        {
            throw new JobExecutionException(e);
        }
    }

    public void setTriggerHandler(TriggerHandler triggerHandler)
    {
        this.triggerHandler = triggerHandler;
    }
}
