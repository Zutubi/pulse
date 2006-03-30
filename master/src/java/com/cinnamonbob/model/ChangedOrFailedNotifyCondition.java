package com.cinnamonbob.model;

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
