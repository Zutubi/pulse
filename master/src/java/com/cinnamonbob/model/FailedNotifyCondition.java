package com.zutubi.pulse.model;

/**
 * 
 *
 */
public class FailedNotifyCondition implements NotifyCondition
{
    public boolean satisfied(BuildResult result)
    {
        return !result.succeeded();
    }
}
