package com.cinnamonbob.scheduling;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobDataMap;

import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.bootstrap.ComponentContext;

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
        // NOTE: When this is executed the first time (on scheduler startup), this throws
        //       an NPE since the triggerHandler has not been constructed in the spring context
        //       being used to handle autowiring...

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
