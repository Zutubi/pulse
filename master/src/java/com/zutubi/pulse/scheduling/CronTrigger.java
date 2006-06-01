package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.model.Project;

/**
 * <class-comment/>
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

    public CronTrigger copy(Project oldProject, Project newProject)
    {
        CronTrigger copy = new CronTrigger();
        copyCommon(copy, oldProject, newProject);
        copy.cron = cron;
        return copy;
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

    public boolean canEdit()
    {
        return true;
    }
}
