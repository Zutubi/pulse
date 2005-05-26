package com.cinnamonbob.core2.schedule;

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
    private String schedule;

    public void activate()
    {
        // do whatever you need to do to activate this trigger.
        Scheduler scheduler = QuartzManager.getScheduler();

        // create new cron trigger, group = project, name = project.recipe.triggerClass

        try
        {
            org.quartz.CronTrigger trigger = new org.quartz.CronTrigger("name", "group", schedule);
            JobDetail job = new JobDetail("name", "group", CallbackJob.class);
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

    public void deactivate()
    {
        Scheduler scheduler = QuartzManager.getScheduler();
        try
        {
            scheduler.unscheduleJob("", "");
        }
        catch (SchedulerException e)
        {
            e.printStackTrace();
        }
    }

    public boolean isActive()
    {
        Scheduler scheduler = QuartzManager.getScheduler();
        int state = org.quartz.Trigger.STATE_NONE;
        try
        {
            state = scheduler.getTriggerState("", "");
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

    public void setSchedule(String s)
    {
        schedule = s;
    }

    /**
     * Trigger
     */
    public void trigger()
    {
        // are we ready to trigger a new build?
        // if (scm.isModified()) {

//        BuildRequest buildRequest = new BuildRequest();
//        buildRequest.setProjectName("");
//        buildRequest.setRecipeName("");

        // BuildManager.dispatch(buildRequest);
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
