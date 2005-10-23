package com.cinnamonbob.scheduling;

import com.cinnamonbob.model.Entity;
import com.cinnamonbob.bootstrap.quartz.QuartzManager;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.ParseException;

import org.quartz.Scheduler;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;

/**
 * <class-comment/>
 */
public class CronTrigger extends Entity implements Trigger
{
    private static final Logger LOG = Logger.getLogger(CronTrigger.class.getName());

    private static final String NAME_PREFIX = "cron";

    private String cronExpression;

    public CronTrigger()
    {

    }

    public CronTrigger(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }

    public String getCronExpression()
    {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }

    public void trigger()
    {
        // TODO implement it!!!!
//        schedule.triggered();
    }

    public void enable()
    {
        // do whatever you need to do to activate this trigger.
        Scheduler scheduler = QuartzManager.getScheduler();

        // create new cron trigger, group = project, name = project.recipe.triggerClass
        try
        {
            String groupName = getGroupName();

            org.quartz.CronTrigger trigger = new org.quartz.CronTrigger(NAME_PREFIX + ".trigger", groupName, cronExpression);
            JobDetail job = new JobDetail(NAME_PREFIX + ".job", groupName, CallbackJob.class);
            job.getJobDataMap().put("self", this);
            scheduler.scheduleJob(job, trigger);
        }
        catch (SchedulerException e)
        {
            LOG.log(Level.SEVERE, "", e);
        }
        catch (ParseException e)
        {
            LOG.log(Level.SEVERE, "", e);
        }
    }

    public void disable()
    {
        Scheduler scheduler = QuartzManager.getScheduler();
        try
        {
            scheduler.unscheduleJob(NAME_PREFIX + ".trigger", getGroupName());
        }
        catch (SchedulerException e)
        {
            LOG.log(Level.SEVERE, "", e);
        }
    }

    public boolean isEnabled()
    {
        Scheduler scheduler = QuartzManager.getScheduler();
        int state = org.quartz.Trigger.STATE_NONE;
        try
        {
            state = scheduler.getTriggerState(NAME_PREFIX + ".trigger", getGroupName());
        }
        catch (SchedulerException e)
        {
            LOG.log(Level.WARNING, "", e);
        }
        switch (state)
        {
            case org.quartz.Trigger.STATE_NORMAL:
            case org.quartz.Trigger.STATE_BLOCKED:
                return true;
        }
        return false;
    }

    public String getType()
    {
        return "cron";
    }

    public String getSummary()
    {
        return cronExpression;
    }

    private String getGroupName()
    {
        return Long.toString(getId());
    }
}
