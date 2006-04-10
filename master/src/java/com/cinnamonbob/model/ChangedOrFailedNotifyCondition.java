package com.zutubi.pulse.model;

/**
 * <class-comment/>
 */
public class ChangedOrFailedNotifyCondition extends CompoundNotifyCondition
{
    public ChangedOrFailedNotifyCondition()
    {
        super(new ChangedNotifyCondition(), new FailedNotifyCondition(), true);
    }
}
