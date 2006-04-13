/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.scheduling;

import org.quartz.Job;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

/**
 * This is used for testing purposes only.
 */
public class QuartzTaskJobFactory implements JobFactory
{
    private TriggerHandler triggerHandler;

    public QuartzTaskJobFactory(TriggerHandler triggerHandler)
    {
        this.triggerHandler = triggerHandler;
    }

    public Job newJob(TriggerFiredBundle bundle) throws SchedulerException
    {
        QuartzTaskCallbackJob task = new QuartzTaskCallbackJob();
        task.setTriggerHandler(triggerHandler);
        return task;
    }
}
