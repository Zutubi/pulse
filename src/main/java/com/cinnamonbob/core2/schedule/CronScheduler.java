package com.cinnamonbob.core2.schedule;

/**
 * 
 *
 */
public class CronScheduler implements Scheduler
{
    private String schedule;

    public void setSchedule(String schedule)
    {
        this.schedule = schedule;
    }
}
