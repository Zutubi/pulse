package com.zutubi.pulse.scheduling;

/**
 * <class-comment/>
 */
public class OneShotTrigger extends SimpleTrigger
{
    public OneShotTrigger()
    {
    }

    public OneShotTrigger(String name, String group)
    {
        super(name, group, null, 0, 0);
    }
}
