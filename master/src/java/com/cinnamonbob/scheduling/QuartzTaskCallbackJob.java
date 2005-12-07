package com.cinnamonbob.scheduling;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobDataMap;

import java.util.logging.Logger;

/**
 * <class-comment/>
 */
public class QuartzTaskCallbackJob implements Job
{
    private static final Logger LOG = Logger.getLogger(QuartzTaskCallbackJob.class.getName());

    public static final String TRIGGER_PROP = "trigger";
    public static final String TASK_PROP = "task";

    private CronSchedulerImpl scheduler;

    public void setScheduler(CronSchedulerImpl scheduler)
    {
        this.scheduler = scheduler;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        // notify schedule manager that this trigger has been activated.
        JobDataMap data = context.getJobDetail().getJobDataMap();
        try
        {
            scheduler.trigger((Trigger) data.get(TRIGGER_PROP), (Task)data.get(TASK_PROP));
        }
        catch (SchedulingException e)
        {
            throw new JobExecutionException(e);
        }
    }


}
