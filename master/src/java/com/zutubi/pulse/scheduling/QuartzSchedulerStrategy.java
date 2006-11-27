package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.util.logging.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * <class-comment/>
 */
public abstract class QuartzSchedulerStrategy implements SchedulerStrategy
{
    private static final Logger LOG = Logger.getLogger(QuartzSchedulerStrategy.class);

    protected static final String CALLBACK_JOB_NAME = "cron.trigger.job.name";

    protected static final String CALLBACK_JOB_GROUP = "cron.trigger.job.group";

    protected static final String QUARTZ_GROUP = Scheduler.DEFAULT_GROUP;

    private Scheduler quartzScheduler;

    private TriggerHandler triggerHandler;

    public void setQuartzScheduler(Scheduler quartzScheduler)
    {
        this.quartzScheduler = quartzScheduler;
    }

    public Scheduler getQuartzScheduler()
    {
        return quartzScheduler;
    }

    public void pause(Trigger trigger) throws SchedulingException
    {
        try
        {
            getQuartzScheduler().pauseTrigger(Long.toString(trigger.getId()), QUARTZ_GROUP);
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
            getQuartzScheduler().resumeTrigger(Long.toString(trigger.getId()), QUARTZ_GROUP);
            trigger.setState(TriggerState.SCHEDULED);
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e);
        }
    }

    public void stop(boolean force)
    {
        try
        {
            // Quartz shceduler stop takes an argument indicating if we
            // should wait for scheduled tasks to complete first (hence the
            // inversion of force).
            getQuartzScheduler().shutdown(!force);
        }
        catch (SchedulerException e)
        {
            LOG.severe("Unable to shutdown Quartz scheduler", e);
        }
    }

    public void unschedule(Trigger trigger) throws SchedulingException
    {
        try
        {
            getQuartzScheduler().unscheduleJob(Long.toString(trigger.getId()), QUARTZ_GROUP);
            trigger.setState(TriggerState.NONE);
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e);
        }
    }

    protected void ensureCallbackRegistered() throws SchedulerException
    {
        JobDetail existingJob = getQuartzScheduler().getJobDetail(CALLBACK_JOB_NAME, CALLBACK_JOB_GROUP);
        if (existingJob == null)
        {
            // register the job detail once only.
            JobDetail detail = new JobDetail(CALLBACK_JOB_NAME, CALLBACK_JOB_GROUP, QuartzTaskCallbackJob.class);
            detail.setDurability(true); // will stay around after the trigger has gone.
            getQuartzScheduler().addJob(detail, true);
        }
    }

    public void init(Trigger trigger) throws SchedulingException
    {
        boolean pause = trigger.isPaused();
        schedule(trigger);
        if(pause)
        {
            pause(trigger);
        }
    }

    public void schedule(Trigger trigger) throws SchedulingException
    {
        try
        {
            // the quartz trigger equivalent...
            org.quartz.Trigger quartzTrigger = createTrigger(trigger);

            // the callback job
            ensureCallbackRegistered();
            quartzTrigger.setJobName(CALLBACK_JOB_NAME);
            quartzTrigger.setJobGroup(CALLBACK_JOB_GROUP);
            quartzTrigger.getJobDataMap().put(QuartzTaskCallbackJob.TRIGGER_PROP, trigger);

            getQuartzScheduler().scheduleJob(quartzTrigger);

            trigger.setState(TriggerState.SCHEDULED);
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e);
        }
    }

    public void setTriggerHandler(TriggerHandler handler)
    {
        this.triggerHandler = handler;
    }

    public boolean dependsOnProject(Trigger trigger, long projectId)
    {
        return false;
    }

    protected abstract org.quartz.Trigger createTrigger(Trigger trigger) throws SchedulingException;
}
