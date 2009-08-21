package com.zutubi.pulse.master.scheduling;

/**
 * A trigger that fires based on a Cron-like expression.
 */
public class CronTrigger extends Trigger
{
    protected static final String TYPE = "cron";

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

    public String getType()
    {
        return TYPE;
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
