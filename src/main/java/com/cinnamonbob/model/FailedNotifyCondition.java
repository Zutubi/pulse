package com.cinnamonbob.model;

import com.cinnamonbob.core.model.BuildResult;
import com.cinnamonbob.core.model.ResultState;

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
