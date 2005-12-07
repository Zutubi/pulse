package com.cinnamonbob.scheduling;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.text.ParseException;

/**
 * <class-comment/>
 */
public class CronSchedulerImpl extends BaseSchedulerImpl
{
    public static final String CALLBACK_JOB_NAME = "cron.trigger.job.name";
    public static final String CALLBACK_JOB_GROUP = "cron.trigger.job.group";

    private Scheduler quartzScheduler;

    public void schedule(Trigger trigger, Task task) throws SchedulingException
    {
        CronTrigger cronTrigger = (CronTrigger) trigger;
        try
        {
            org.quartz.Trigger quartzTrigger = new org.quartz.CronTrigger(cronTrigger.getName(), cronTrigger.getGroup(), cronTrigger.getCron());

            JobDetail detail = new JobDetail(CALLBACK_JOB_NAME, CALLBACK_JOB_GROUP, QuartzTaskCallbackJob.class);
            detail.getJobDataMap().put(QuartzTaskCallbackJob.TRIGGER_PROP, trigger);
            detail.getJobDataMap().put(QuartzTaskCallbackJob.TASK_PROP, task);
            quartzScheduler.scheduleJob(detail, quartzTrigger);
            trigger.setState(TriggerState.ACTIVE);
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e);
        }
        catch (ParseException e)
        {
            throw new SchedulingException(e);
        }
    }

    public void unschedule(Trigger trigger) throws SchedulingException
    {
        try
        {
            quartzScheduler.unscheduleJob(trigger.getName(), trigger.getGroup());
            trigger.setState(TriggerState.NONE);
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e);
        }
    }

    public void pause(Trigger trigger) throws SchedulingException
    {
        try
        {
            quartzScheduler.pauseTrigger(trigger.getName(), trigger.getGroup());
            trigger.setState(TriggerState.PAUSED);
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e);
        }
    }

    public void resume(Trigger trigger) throws SchedulingException
    {
        try
        {
            quartzScheduler.resumeTrigger(trigger.getName(), trigger.getGroup());
            trigger.setState(TriggerState.ACTIVE);
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e);
        }
    }

    public void setQuartzScheduler(Scheduler quartzScheduler)
    {
        this.quartzScheduler = quartzScheduler;
    }
}
