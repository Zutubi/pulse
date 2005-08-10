package com.cinnamonbob.model;

import com.cinnamonbob.core.BuildResult;

/**
 * 
 *
 */
public class FailedNotifyCondition implements NotifyCondition
{
    public boolean satisfied(BuildResult result)
    {
        return false;
    }
}
