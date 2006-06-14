package com.zutubi.pulse.model;

/**
 * THe build was requested by a trigger (cron, scm...)
 */
public class TriggerBuildReason extends AbstractBuildReason
{
    private String triggerName;

    public TriggerBuildReason()
    {
    }

    public TriggerBuildReason(String triggerName)
    {
        this.triggerName = triggerName;
    }

    public String getSummary()
    {
        return "trigger '" + triggerName + "'";
    }

    public String getTriggerName()
    {
        return triggerName;
    }

    private void setTriggerName(String triggerName)
    {
        this.triggerName = triggerName;
    }
}
