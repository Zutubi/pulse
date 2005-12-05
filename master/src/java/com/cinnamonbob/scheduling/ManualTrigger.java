package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class ManualTrigger extends Trigger
{
    public ManualTrigger()
    {

    }
    public ManualTrigger(String name)
    {
        this(name, null);
    }
    public ManualTrigger(String name, String group)
    {
        super(name, group);
    }
}
