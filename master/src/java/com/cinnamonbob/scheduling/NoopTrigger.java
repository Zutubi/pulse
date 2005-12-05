package com.cinnamonbob.scheduling;

/**
 * <class-comment/>
 */
public class NoopTrigger extends Trigger
{
    public NoopTrigger()
    {

    }

    public NoopTrigger(String name)
    {
        this(name, null);
    }

    public NoopTrigger(String name, String group)
    {
        super(name, group);
    }
}
