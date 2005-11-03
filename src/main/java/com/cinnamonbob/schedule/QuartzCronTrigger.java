package com.cinnamonbob.schedule;

import com.cinnamonbob.core.model.Entity;
import static com.cinnamonbob.schedule.QuartzSupport.WRAPPER_GROUP;
import static com.cinnamonbob.schedule.QuartzSupport.WRAPPER_NAME;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.text.ParseException;

/**
 * <class-comment/>
 */
public class QuartzCronTrigger extends Entity implements Trigger
{
    private Scheduler scheduler;

    public void setQuartzScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    private String cronExpression;

    public QuartzCronTrigger()
    {

    }

    public QuartzCronTrigger(String expression)
    {
        // ensure that expression is valid.
        this.cronExpression = expression;
    }

    // register trigger with quartz scheduler...
    public void enable() throws SchedulingException
    {
        try
        {
            //TODO: need unique name for the cron trigger. name | project concatination.
            CronTrigger trigger = new CronTrigger(Long.toString(getId()), null, cronExpression);
            trigger.getJobDataMap().put("id", getId());
            trigger.setJobName(WRAPPER_NAME);
            trigger.setJobGroup(WRAPPER_GROUP);

            scheduler.scheduleJob(trigger);
        }
        catch (SchedulerException e)
        {
            throw new SchedulingException(e);
        }
        catch (ParseException e)
        {
            throw new IllegalStateException("Attempting to enable invalid quartz cron trigger.");
        }
    }

    public String getCronExpression()
    {
        return cronExpression;
    }

    private void setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }
}
