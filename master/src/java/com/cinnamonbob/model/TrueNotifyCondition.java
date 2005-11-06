package com.cinnamonbob.model;

import com.cinnamonbob.core.model.BuildResult;

public class TrueNotifyCondition implements NotifyCondition
{
    public boolean satisfied(BuildResult result)
    {
        return true;
    }

}