package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class CronTrigger extends Trigger
{
    private String cron;

    public CronTrigger()
    {

    }

    public CronTrigger(String cron, String name)
    {
        this(cron, name, DEFAULT_GROUP);
    }

    public CronTrigger(String cron, String name, String group)
    {
        super(name, group);
        this.cron = cron;
    }

    public String getCron()
    {
        return cron;
    }

    public void setCron(String cron)
    {
        this.cron = cron;
    }
}
