package com.cinnamonbob.model;

/**
 * 
 *
 */
public class FailedNotifyCondition implements NotifyCondition
{
    public boolean satisfied(BuildResult result)
    {
        return result.getState() != ResultState.SUCCESS;
    }
}
