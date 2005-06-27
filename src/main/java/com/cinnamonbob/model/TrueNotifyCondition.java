package com.cinnamonbob.model;

import com.cinnamonbob.core2.BuildResult;

public class TrueNotifyCondition implements NotifyCondition
{
    public boolean satisfied(BuildResult result)
    {
        return true;
    }

}