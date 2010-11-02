package com.zutubi.pulse.master.scheduling;

import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.util.logging.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import static com.zutubi.pulse.master.scheduling.QuartzTaskCallbackJob.SOURCE_PROP;

/**
 * The QuartzSchedulerStrategy is an implementation of the SchedulerStrategy
 * interface backed by the quartz scheduler.
 *
 * Any triggers scheduled with this scheduler are passed through to the underlying
 * quartz scheduler.  When those quartz triggers fire, they call the
 * {@link com.zutubi.pulse.master.scheduling.QuartzTaskCallbackJob} which converts
 * the callback into a local trigger.
 */
public abstract class QuartzSchedulerStrategy implements SchedulerStrategy
{
    private static final Logger LOG = Logger.getLogger(QuartzSchedulerStrategy.class);

    protected static final String CALLBACK_JOB_NAME = "cron.trigger.job.name";

    protected static final String CALLBACK_JOB_GROUP = "cron.trigger.job.group";

    private Scheduler quartzScheduler;
    private ObjectFactory objectFactory;

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
            // if trigger is not scheduled, then schedule it first before pausing.
            org.quartz.Trigger t = getQuartzScheduler().getTrigger(trigger.getName(), trigger.getGroup());
            if (t == null)
            {
                schedule(trigger);
            }
            getQuartzScheduler().pauseTrigger(trigger.getName(), trigger.getGroup());
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
            getQuartzScheduler().resumeTrigger(trigger.getName(), trigger.getGroup());
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
            // Quartz scheduler stop takes an argument indicating if we
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
            getQuartzScheduler().unscheduleJob(trigger.getName(), trigger.getGroup());
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
        if (trigger.isScheduled())
        {
            schedule(trigger);
            if(trigger.isPaused())
            {
                pause(trigger);
            }
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

            QuartzTaskCallbackTriggerSource source = objectFactory.buildBean(QuartzTaskCallbackTriggerSource.class, new Class[]{Trigger.class}, new Object[]{trigger});
            quartzTrigger.getJobDataMap().put(SOURCE_PROP, source);

            getQuartzScheduler().scheduleJob(quartzTrigger);
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e);
        }
    }

    public void setTriggerHandler(TriggerHandler handler)
    {
        // noop.
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    protected abstract org.quartz.Trigger createTrigger(Trigger trigger) throws SchedulingException;
}
