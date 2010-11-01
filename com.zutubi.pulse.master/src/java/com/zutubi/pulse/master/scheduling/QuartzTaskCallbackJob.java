package com.zutubi.pulse.master.scheduling;

import com.zutubi.util.logging.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * All of the triggers scheduled within the Quartz scheduler
 * trigger this job.  This job then examines the triggers
 * job data map and uses its contents to determine which
 * pulse trigger is associated with the callback. 
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
