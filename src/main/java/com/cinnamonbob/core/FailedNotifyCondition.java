package com.cinnamonbob.core;

public class FailedNotifyCondition implements NotifyCondition
{

    public boolean satisfied(BuildResult result)
    {
        return result.succeeded() == false;
    }

}
