package com.zutubi.pulse.model;

public class TrueNotifyCondition implements NotifyCondition
{
    public boolean satisfied(BuildResult result)
    {
        return true;
    }

}