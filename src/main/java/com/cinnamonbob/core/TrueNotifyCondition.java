package com.cinnamonbob.core;

public class TrueNotifyCondition implements NotifyCondition
{
    public boolean satisfied(BuildResult result)
    {
        return true;
    }

}
