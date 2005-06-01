package com.cinnamonbob.core.schedule;

import com.cinnamonbob.bootstrap.quartz.QuartzManager;
import org.quartz.*;

import java.text.ParseException;
import java.util.Map;

/**
 * 
 *
 */
public class CronTrigger implements Trigger
{
    private Schedule schedule;
    private String cronSchedule;
    
    private static final String NAME_PREFIX = "cron";
    
    public void setSchedule(String cronSchedule)
    {
        this.cronSchedule = cronSchedule;    
    }
    
    public void setSchedule(Schedule schedule)
    {
        this.schedule = schedule;
    }

    public void trigger()
    {
        schedule.triggered();
    }

    public void enable()
    {
        // do whatever you need to do to activate this trigger.
        Scheduler scheduler = QuartzManager.getScheduler();

        // create new cron trigger, group = project, name = project.recipe.triggerClass
        try
        {
            String groupName = getGroupName();
            
            org.quartz.CronTrigger trigger = new org.quartz.CronTrigger(NAME_PREFIX + ".trigger", groupName, cronSchedule);
            JobDetail job = new JobDetail(NAME_PREFIX + ".job", groupName, CallbackJob.class);
            job.getJobDataMap().put("self", this);
            scheduler.scheduleJob(job, trigger);
        }
        catch (SchedulerException e)
        {
            e.printStackTrace();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
        switch (state)
        {
            case org.quartz.Trigger.STATE_NORMAL:
            case org.quartz.Trigger.STATE_BLOCKED:
                return true;
        }
        return false;
    }
    
    private String getGroupName()
    {
        return schedule.getProject().getName();
    }
}

/**
 * Simple request to build a job.
 */
class CallbackJob implements Job
{
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
    {
        Map dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        Trigger callback = (Trigger) dataMap.get("self");
        callback.trigger();
    }
}