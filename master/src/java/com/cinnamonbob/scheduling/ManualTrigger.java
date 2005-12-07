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
        this(name, DEFAULT_GROUP);
    }

    public ManualTrigger(String name, String group)
    {
        super(name, group);
    }
}
