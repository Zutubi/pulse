package com.cinnamonbob.schedule.triggers;

import static com.cinnamonbob.schedule.QuartzSupport.*;
import com.cinnamonbob.schedule.SchedulingException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.text.ParseException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * <class-comment/>
 */
public class CronTrigger extends Trigger
{
    private static final Logger LOG = Logger.getLogger(CronTrigger.class.getName());

    private static final String CRON_PROPERTY = "scheduler.cron.expression";

    private Scheduler scheduler;

    public void setQuartzScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public CronTrigger()
    {
        // no arg constructor required by hibernate.
    }

    public CronTrigger(String cron)
    {
        setCron(cron);
    }

    /**
     * The next time this trigger is scheduled to activate.
     *
     * @return the next time this trigger will activate, or null if it will not.
     */
    public Date getNextTriggerTime() throws SchedulingException
    {
        try
        {
            if (!isActive())
            {
                return null;
            }

            return scheduler.getTrigger(getQuartzTriggerName(), TRIGGER_GROUP).getNextFireTime();
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e.getMessage(), e);
        }
    }

    public void trigger()
    {
        super.trigger();
    }

    public void rehydrate() throws SchedulingException
    {
        if (isActive())
        {
            internalActivate();
        }
        else if (isPaused())
        {
            // can we go directly to paused with the quartz trigger?
            internalActivate();
            internalPause();
        }
    }

    public void internalActivate() throws SchedulingException
    {
        try
        {
            // activate this cron trigger.
            org.quartz.CronTrigger quartzTrigger = new org.quartz.CronTrigger(getQuartzTriggerName(), TRIGGER_GROUP, getCron());
            quartzTrigger.setJobName(WRAPPER_NAME);
            quartzTrigger.setJobGroup(WRAPPER_GROUP);
            scheduler.scheduleJob(quartzTrigger);
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e);
        }
        catch (ParseException e)
        {
            // should never end up here, implies that the cron expression is invalid.
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void internalComplete() throws SchedulingException
    {
        try
        {
            scheduler.unscheduleJob(getQuartzTriggerName(), TRIGGER_GROUP);
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e);
        }
    }

    public void internalPause() throws SchedulingException
    {
        try
        {
            scheduler.pauseTrigger(getQuartzTriggerName(), TRIGGER_GROUP);
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e);
        }
    }

    public void internalResume() throws SchedulingException
    {
        try
        {
            scheduler.resumeTrigger(getQuartzTriggerName(), TRIGGER_GROUP);
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e);
        }
    }

    private String getQuartzTriggerName()
    {
        return Long.toOctalString(getId());
    }

    /**
     * Retrieve the cron expression that defines when this trigger will trigger
     * the execution of the associated task.
     *
     * @return the cron expression string
     */
    public String getCron()
    {
        return (String) getProperties().get(CRON_PROPERTY);
    }

    // for hibernate.
    private void setCron(String cron)
    {
        getProperties().put(CRON_PROPERTY, cron);
    }
}
